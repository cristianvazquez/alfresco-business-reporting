/**
 * Copyright (C) 2011 - 2013 Alfresco Business Reporting project
 *
 * This file is part of the Alfresco Business Reporting project.
 *
 * Licensed under the GNU LGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.gnu.org/licenses/lgpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.alfresco.reporting.action.executer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.reporting.Constants;
import org.alfresco.reporting.ReportingException;
import org.alfresco.reporting.ReportingHelper;
import org.alfresco.reporting.ReportingModel;
import org.alfresco.reporting.execution.ReportTemplate;
import org.alfresco.reporting.execution.ReportingContainer;
import org.alfresco.reporting.execution.ReportingRoot;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Action ReportContainerExecuter is executed against a TYPE_REPORTING_REPORTING_CONTAINER or a
 * TYPE_REPORTING_REPORTTEMPLATE. If the input appears a Container, execute the reports in 
 * each of the child ReportTemplate documents. If the input appears a specific 
 * Report, then all children of the container are cycled, but the only one matching 
 * TYPE_REPORTING_REPORT.getNodeREf() will be executed.  
 *  
 * For each result, get all children. 
 *   Execute a report against each of the children (ReportExecuter)
 *     Parameterize if parameters are defined.
 *      
 * Works only in the structure
 * 
 * T:reporting:reportingRoot
 *   T:reporting:reportingContainer
 *     T:reporting:report
 *     
 * @author tpeelen
 *
 */
public class ReportContainerExecutor extends ActionExecuterAbstractBase {

	private ActionService actionService;
	private FileFolderService fileFolderService;
	private NodeService nodeService;
	private SearchService searchService;
	private ReportingHelper reportingHelper;

	public static final String REPORTING_CONTAINER_NODEREF = "reportingContainerRef";
	public static final String REPORTING_ROOT_NODEREF = "reportingRootRef";
	public static final String NAME = "report-container-executer";
	
	private static Log logger = LogFactory.getLog(ReportContainerExecutor.class);
	
	/**
	 * This is where the action is.
	 */
	@Override
	protected void executeImpl(Action action, NodeRef reportingContainerRef) {
		
		//is filled only if a single report is executed instead of all reports in a container
		NodeRef specificReportRef=null; 
		
		if (logger.isDebugEnabled())
			logger.debug("enter executeImpl, getting a " +
					nodeService.getType(reportingContainerRef));
		
		// fail if there is no valid noderef, or it is not a (Report or ReportingContainer)
		if (reportingContainerRef==null){
			throw new AlfrescoRuntimeException("not a valid NodeRef");
		}
		if (nodeService.hasAspect(reportingContainerRef, 
										ReportingModel.ASPECT_REPORTING_CONTAINERABLE) ||
					nodeService.hasAspect(reportingContainerRef, 
										ReportingModel.ASPECT_REPORTING_REPORTABLE)){
				//lets roll!
		
		
			if (nodeService.hasAspect(reportingContainerRef, 
											ReportingModel.ASPECT_REPORTING_REPORTABLE)){
				// capture the current noderef the logic was executed against
				specificReportRef=reportingContainerRef;
				
				// find the parent container, in order to comply with existing logic and execute
				// only the relevant chil in the context of the Container
				reportingContainerRef = reportingHelper.getReportingContainer(specificReportRef);
			}
			
			// Create a ReportingContainer object
			ReportingContainer reportingContainer = new ReportingContainer(reportingContainerRef);
			reportingHelper.initializeReportingContainer(reportingContainer);
			
			// Create a ReportingRoot object
			ReportingRoot reportingRoot = 
					new ReportingRoot(
							reportingHelper.getReportingRoot(reportingContainer.getNodeRef()));
			reportingHelper.initializeReportingRoot(reportingRoot);
			
			// cycle all child elements of the container (should be reports)
			List<ChildAssociationRef> childList = nodeService.getChildAssocs(reportingContainerRef);
			Iterator<ChildAssociationRef> childIterator = childList.iterator();
			while (childIterator.hasNext()) {
				ChildAssociationRef ref = childIterator.next();
				
				// Get the NodeRef of each Child element. 
				NodeRef reportRef = ref.getChildRef();
				
				// Create a ReportTemplate object
				ReportTemplate report = new ReportTemplate(reportRef);
				reportingHelper.initializeReport(report);
				
				//logger.debug("executeImpl: Subst = " + report.getSubstitution());
				
				
				// validate if document is a report
				if (report.isReportingDocument()){
					// it is a report object
					logger.debug("executeImpl: Report name = " + report.getName());
					
					if (specificReportRef!=null){
						// only execute if the current NodeRef == specificReportRef
						if (report.getNodeRef().equals(specificReportRef)){
							processReport(report, reportingContainer, reportingRoot);
						}
					} else {
					// deal with the reportable
						processReport(report, reportingContainer, reportingRoot);
					}
					
				} // end if isDocumentRef
			} // end while
			
		} // end if lets roll	
		if (logger.isDebugEnabled())
			logger.debug("exit executeImpl");
	}

	
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {

	}
	
	private String processDateElementsInPath(String folderPath){
		if (folderPath!=null){
			logger.debug("processDateElementsInFolderPath index=" + folderPath.indexOf("${"));
			while (folderPath.indexOf("${")>-1){
				String folderStart = folderPath.substring(0,folderPath.indexOf("${")); 
				String datemask    = folderPath.substring(folderPath.indexOf("${")+2, folderPath.indexOf("}")); 
				String folderEnd   = folderPath.substring(folderPath.indexOf("}")+1);
				logger.debug("processDateElementsInFolderPath datemask="+datemask);
				SimpleDateFormat sdf = new SimpleDateFormat(datemask);
				
				folderPath = folderStart + sdf.format(new Date()) + folderEnd;
			} // end while
		} // end if folderPath!=null
		logger.debug("processDateElementsInFolderPath returning "+ folderPath);
		return folderPath;
	}
	
	private void processReport(	final ReportTemplate report, 
								final ReportingContainer reportingContainer, 
								final ReportingRoot reportingRoot){
		Properties keyValues= new Properties();
		
		
		if (logger.isDebugEnabled())
			logger.debug("enter processReport, report="+report.getName());
		String targetPath = report.getTargetPath();
		Properties targetQueries = reportingRoot.getTargetQueries();
		
		
		
		// 1. Determine/get/create target Path
		//    Lets see if there is a relative distribution by Site
		if (targetPath!=null){
			targetPath = targetPath.trim();
			if ((targetPath.indexOf("${")==0) && (targetPath.indexOf("}")>-1)){
				// 1a. it is a distribution by Site or other placeholder
				logger.debug("processReportable: it is a distribution by container");
				
				// get the target container query key
				String placeHolder = targetPath.substring(2,targetPath.indexOf("}"));
				String relativePath= targetPath.substring(targetPath.indexOf("}")+1);
				
				relativePath = processDateElementsInPath(relativePath);
				
				logger.debug("  placeholder:   " + placeHolder);
				logger.debug("  relative path: " + relativePath);
				
				if (placeHolder!=null)
				{
					
					if (targetQueries.containsKey(placeHolder)){
						// execute Lucene query. For each result find node.
						//   For each node, append relative path
						//     get or create this path
						//       execute ReportExecuter against each of these end nodes (in proper output format)
						logger.debug("processReport: Processing with placeholder: " + placeHolder);
						String placeHolderQuery = 
								targetQueries.getProperty(placeHolder);
						String searchLanguage = 
								reportingHelper.getSearchLanguage(reportingRoot.getRootQueryLanguage());
						
						logger.debug("processReport: query2="+ placeHolderQuery + "("+searchLanguage+")");
						
						ResultSet placeHolderResults = searchService.query(
									StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, 
									searchLanguage, 
									placeHolderQuery);
														
						
						
						// cycle the resultset of containers
						for (ResultSetRow placeHolderRow : placeHolderResults){
							final NodeRef targetRootRef = placeHolderRow.getChildAssocRef().getChildRef();
							
							logger.debug("Found targetRoot: " + 
										nodeService.getProperty(targetRootRef, ContentModel.PROP_NAME));
							
							// Introduce a storageNodeRef. This is used if you do a query against 
							// persons. The resulting report will be stored in the UserHome space.
							// The value substitution will however be executed against the Person object.
							NodeRef storageNodeRef = targetRootRef;
							logger.debug("processReport: storageNodeRef before=" + storageNodeRef);
							
							try{
								// if a Person object is found, replace it by its HomeFolder. For the best fun!
								if (nodeService.getType(targetRootRef).equals(ContentModel.TYPE_PERSON) ){
									logger.debug("processReport: The value="+nodeService.getProperty(targetRootRef, ContentModel.PROP_HOMEFOLDER));
									
									if (nodeService.getProperty(targetRootRef, ContentModel.PROP_HOMEFOLDER)!=null){
										logger.debug("processReport: createGetRepositoryPath: SWAPPING Person for UserHome");
										storageNodeRef = (NodeRef)nodeService.getProperty(targetRootRef, ContentModel.PROP_HOMEFOLDER);
										logger.debug("processReport: createGetRepositoryPath: storageNodeRef after swap="+ storageNodeRef);
									} else {
										logger.debug("processReport: createGetRepositoryPath: ow boy, no UserHome available for user " + 
												nodeService.getProperty(targetRootRef, ContentModel.PROP_USERNAME));
										throw new ReportingException("No UserHome found for user " + 
												nodeService.getProperty(targetRootRef, ContentModel.PROP_USERNAME));
									}
								} else {
									logger.debug("createGetRepositoryPath: no SWAPPING");
								}
							} catch (ReportingException re) {
								logger.fatal("processReport: User without a UserHome... Silent ignore");
							}
							logger.debug("processReport: processReportstorageNodeRef fully after=" + storageNodeRef);
							
							
							// keyValues now contains the keys and the related short-form qnames
							keyValues = report.getSubstitution();
							
							logger.debug("processReport: initial keyValues = " + keyValues);
							
							// process of *replacing* the short-form qnames into actual 
							// node-property values
							Enumeration keys = keyValues.keys();
							while (keys.hasMoreElements()){
								final String key = (String)keys.nextElement();
								final String value = keyValues.getProperty(key,"");
								logger.debug("Initial key=value; " + key + "=" + value);
								
								if (value.contains(":") && !"".equals(value)) {
									// system property
									final QName property = reportingHelper.replaceShortQNameIntoLong(value);
									logger.debug("processReport: QName=" + property);
									logger.debug("processReport: key="+key+" value=" + nodeService.getProperty(targetRootRef, property) );
									
									final String propertyValue = nodeService.getProperty(targetRootRef, property).toString();
									logger.debug("processReport: propertyValue=" + propertyValue);
									keyValues.setProperty(key, propertyValue);
								} else {
									// ordinary property
									keyValues.setProperty(key, value);
									logger.debug("processReport: key="+key+" value=" + value + " targetRootRef="+ targetRootRef);
								} // end if/else
							} // end while
							logger.debug("processReport: final keyValues = " + keyValues);
									
							final NodeRef targetRef = createGetRepositoryPath(storageNodeRef, relativePath);
							
							logger.debug("processReport: Found full path: " + nodeService.getPath(targetRef));
							logger.debug("processReport: keyValues = " + keyValues);
							createExecuteReport(targetRef, report, keyValues);
							
						} // end for ResultSetRow
						
						
					} else {
						// we cannot deal with this unknown placeholder.
						// silently let go?!
						logger.warn("Cannot deal with placeholder: " + placeHolder);
						
					} // end if/else targetQueries.containsKey
				} // end if matcher.find
				
			} else {
				// 1b. it is a single fixed path. Start at companyhome, and find your way in
				//NodeRef targetRef = createGetRepositoryPath(targetRootRef, relativePath);
				
				// execute ReportExecuter against each of these end nodes (in proper output format)
				
			}
		} // end if targetPath !=null
		
		if ( (report.getTargetNode()!=null) && (!"".equals(report.getTargetNode())) ) {
			NodeRef newTarget = null;
			// create option to concatinate targetPath to targetNode
			if ((report.getTargetPath()!=null) && (!"".equals(report.getTargetPath()))){
				final String relativePath = processDateElementsInPath(report.getTargetPath());
				newTarget = createGetRepositoryPath(report.getTargetNode(), relativePath);
			} else {
				newTarget = report.getTargetNode();
			}
			logger.debug("executing fixed output path");
			logger.debug("  report    : " + report.getName());
			logger.debug("  targetNode: " + newTarget);
			// keyValues now contains the keys and the related short-form qnames
			keyValues = report.getSubstitution();
			
			logger.debug("processReport: initial keyValues = " + keyValues);
			
			// process of replacing the short-form qnames into actual 
			
			// node-property values
			Enumeration keys = keyValues.keys();
			while (keys.hasMoreElements()){
				String key = (String)keys.nextElement();
				String value = keyValues.getProperty(key,"");
				
				// ordinary property
				keyValues.setProperty(key, value);
				logger.debug("processReport: key="+key+" value=" + value);
			}
			logger.debug("processReport: final keyValues = " + keyValues);
			
			
			createExecuteReport(	newTarget,
									report,
									keyValues);
		} // end if targetPath != null
		
				
		if (logger.isDebugEnabled())
				logger.debug("exit processReportable");
	}
		
	/** 
	 * Create or get the target document (eventually) containing the report output 
	 * @param parentRef
	 * @param reportRef
	 */
	private void createExecuteReport(final NodeRef parentRef, final ReportTemplate report, final Properties keyValues){
		// create new target node, or update existing node (report.pdf or report.xls)
		
		logger.debug("enter createExecuteReport");
		String filename = getOutputFilename(report);
		filename = processDateElementsInPath(filename);
		
		logger.debug("createExecuteReport: just before createGetFilename, parentRef=" + parentRef + " filename="+filename);
		NodeRef targetDocumentRef = createGetFilename(parentRef, filename, report.isOutputVersioned());
		
		// add a marker that this is a report (so you can exclude it from your reporting query...)
		nodeService.addAspect(targetDocumentRef, ReportingModel.ASPECT_REPORTING_EXECUTIONRESULT, null);
		
		// Set MimeType somewhere here TJARDA 20130218
		// @todo
		logger.debug("createExecuteReport: just before executeReportExecuter, ");
		logger.debug("  targetDocumentRef = "+targetDocumentRef);
		logger.debug("  keyValues         = "+keyValues);
		
		
		executeReportExecuter(	report, 
								targetDocumentRef, 
								keyValues);
		
		logger.debug("exit createExecuteReport");
	}
	

	
	/**
	 * get the filename of the produced report. Will be the name of the reportdefinition
	 * with the extension as defined by the outputType. Extensions are defined in the
	 * alfresco-global.properties
	 *   
	 * @param childRef
	 * @return the new filename
	 */
	private String getOutputFilename(final ReportTemplate report){
		String outputType = report.getOutputFormat();
		String filename   = report.getName();
		
		filename = filename.substring(0,filename.lastIndexOf(".")+1);
		
		// get the reportingRootRef. It carries the file extensions
		NodeRef reportingRootRef = reportingHelper.getReportingRoot(report.getNodeRef());
		
		ReportingRoot reportingRoot = new ReportingRoot(reportingRootRef);
		reportingHelper.initializeReportingRoot(reportingRoot);
		
		logger.debug("RootRef name="+ reportingRoot.getName());
		
		String ext="";
		if (outputType.equalsIgnoreCase("excel")){
			ext = reportingRoot.getOutputExtensionExcel();
		} else {
			ext = reportingRoot.getOutputExtensionPdf();
		}
		filename += ext;
		
		return filename;
	}
	
	/**
	 * Create a new document or get the existing document 
	 *    for a given parent
	 *    with a given filename
	 * @param targetRef
	 * @param filename
	 * @return the noderef of the (potentially new) child document
	 */
	private NodeRef createGetFilename(final NodeRef parentRef, final String filename, final boolean versioned){
		if (logger.isDebugEnabled())
			logger.debug("enter createGetFilename: Filename=" + filename);
		
		NodeRef childRef = getChildDocument(parentRef, filename);
		if (childRef==null){
			childRef = fileFolderService.create(parentRef, filename, ContentModel.TYPE_CONTENT).getNodeRef();
			
			if (versioned && !nodeService.hasAspect(childRef, ContentModel.ASPECT_VERSIONABLE)){
				nodeService.addAspect(childRef, ContentModel.ASPECT_VERSIONABLE, null);
			}
			
			logger.debug("  created document   " + filename);
		} else {
			logger.debug("  retrieved document " + filename);
		}
		
		if (logger.isDebugEnabled())
			logger.debug("exit createGetFilename" );
		return childRef;
	}
	
	 private NodeRef getChildDocument(final NodeRef nodeRef, final String filename){
		 NodeRef returnRef=null;
			
			List<FileInfo> fileInfoList = fileFolderService.listFiles(nodeRef);
			for (FileInfo fileInfo : fileInfoList){
				if (fileInfo.getName().equals(filename)){
					returnRef=fileInfo.getNodeRef();
					break;
				}
			}
			return returnRef;
	 }
	 
	/**
	 * Get the Noderef of the targetPath. If the path does not exist, create
	 * the missing spaces (as system user)
	 * 
	 * @param targetPath
	 * @return
	 */
	public NodeRef createGetRepositoryPath(final NodeRef targetRootRef, final String relativePath){
		if (logger.isDebugEnabled())
				logger.debug("enter createGetRepositoryPath: relativePath=" + relativePath);
		NodeRef returnRef = AuthenticationUtil.runAs(new RunAsWork<NodeRef>() {
			public  NodeRef doWork() throws Exception {
				NodeRef returnRef = targetRootRef;
				// construct-or-get the path with the post-fix of the targetPath
				String[] path = relativePath.split("/");
				
				logger.debug("createGetRepositoryPath path elements=" + path.length);
				
				// deal with starting slash
				// deal with starting CompanyHome
				for (String element : path){
					if ((element!=null) && (element.length()>0)){
						// subst. the placeholder with actual path
						
						logger.debug("createGetRepositoryPath current element=" + element);
						
						NodeRef childRef = getChildFolder(returnRef, element);
						if (childRef==null){
							childRef = fileFolderService.create(returnRef, element, ContentModel.TYPE_FOLDER).getNodeRef();
							logger.debug("  created folder   " + element);
						} else {
							logger.debug("  retrieved folder " + element);
						}
						returnRef = childRef;
					}
				} // end for element:path
				return returnRef;
			} // end public doWork
		}, AuthenticationUtil.getSystemUserName());
		if (logger.isDebugEnabled()) 
				logger.debug("exit createGetRepositoryPath: returning: " + returnRef );
		return returnRef;
	}
	
	
	private NodeRef getChildFolder(final NodeRef nodeRef, final String childName){
		NodeRef returnRef=null;
		logger.debug("enter getChildFolder with nodeRef="+nodeRef + " childName="+childName);
		List<FileInfo> fileInfoList = fileFolderService.listFolders(nodeRef);
		for (FileInfo fileInfo : fileInfoList){
			if (fileInfo.getName().equalsIgnoreCase(childName)){
				returnRef=fileInfo.getNodeRef();
				logger.debug("getChildFolder: We are in the break!");
				break;
			}
		}
		return returnRef;
	}
	
	/**
	 * Execute the single report
	 * 
	 * @param reportRef the actual report
	 * @param targetDocumentRef the new documnt to store the report into
	 * @param outputType the type of report to generate (pdf or Excel)
	 */
	private void executeReportExecuter(final ReportTemplate report, final NodeRef targetDocumentRef, final Properties keyValues){
		logger.debug("enter executeReportExecuter");
		logger.debug("  reportRef        : " + report.getNodeRef());
		logger.debug("  reportName       : " + report.getName());
		logger.debug("  targetDocumentRef: " + targetDocumentRef);
		logger.debug("  outputType       : " + report.getOutputFormat());
		logger.debug("  parameters       : " + keyValues);
		
		Action action = actionService.createAction(
					ReportExecuter.NAME);
		action.setParameterValue(
					ReportExecuter.OUTPUT_TYPE, report.getOutputFormat());
		action.setParameterValue(
					ReportExecuter.TARGET_DOCUMENT, targetDocumentRef);
		
		if (keyValues.size()>0){
			action.setParameterValue(ReportExecuter.SEPARATOR, Constants.SEPARATOR);
			
			Enumeration keys = keyValues.keys();
			
			//Properties namespaces = reportingHelper.getNameSpacesShortToLong();
			/*
			Collection<String> nameSpaceKeys = namespaceService.getURIs();
			for (String myKey:nameSpaceKeys){
				String myValue = namespaceService.getNamespaceURI(myKey);
				logger.debug("Found value: "+ myValue+"  namespacekey: " + myKey);
			}
			*/
			
			int i=0;
			while (keys.hasMoreElements()){
				i++;
				String key = (String)keys.nextElement();
				
				String pushString = key + Constants.SEPARATOR + keyValues.getProperty(key,"");
				
				logger.debug("Setting report parameter " + key +" = " + keyValues.getProperty(key,""));
								
				if (i==1)
					action.setParameterValue(ReportExecuter. PARAM_1, pushString);
				if (i==2)
					action.setParameterValue(ReportExecuter. PARAM_2, pushString);
				if (i==3)
					action.setParameterValue(ReportExecuter. PARAM_3, pushString);
				if (i==4)
					action.setParameterValue(ReportExecuter. PARAM_4, pushString);
				
			} // end while key.hasMoreElements
		}
		
		actionService.executeAction(action, report.getNodeRef());
		logger.debug("Exit executeReportExecuter");
	}
	
		
	public void setNodeService(NodeService nodeService)	{
	    this.nodeService = nodeService;
	}
	
	public void setSearchService(SearchService searchService)	{
	    this.searchService = searchService;
	}
	
	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}
	
	public void setActionService(ActionService actionService) {
		this.actionService = actionService;
	}

	
	public void setReportingHelper(ReportingHelper reportingHelper) {
		this.reportingHelper = reportingHelper;
	}
}
