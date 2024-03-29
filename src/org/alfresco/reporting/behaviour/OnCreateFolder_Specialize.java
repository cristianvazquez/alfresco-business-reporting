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

package org.alfresco.reporting.behaviour;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.reporting.ReportingModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OnCreateFolder_Specialize implements OnCreateNodePolicy {

	protected PolicyComponent policyComponent;
	protected NodeService nodeService; 
	
	private static Log logger = LogFactory.getLog(OnCreateFolder_Specialize.class);
	
	public void initialise() {
		this.policyComponent.bindClassBehaviour(QName.createQName(
				NamespaceService.ALFRESCO_URI, "onCreateNode"),
				ContentModel.TYPE_FOLDER,
				new JavaBehaviour(this, "onCreateNode",	NotificationFrequency.FIRST_EVENT));
	}
	
	
	@Override
	public void onCreateNode(ChildAssociationRef car) {
		NodeRef parent = car.getParentRef();
		NodeRef child = car.getChildRef();
		
		try{
			if (nodeService.hasAspect(parent, 
									ReportingModel.ASPECT_REPORTING_REPORTING_ROOTABLE)){
				// its for us! We're in a folder marked Reporting folder. 
				// (Most likely in Data Dictionary) => Specialize!
				nodeService.setType(child, 	
									ReportingModel.TYPE_REPORTING_CONTAINER);
			}
		} catch (Exception e){
			// lets be tolerant. The world remains spinning if this 
			// specialization does not work out well. Can be dome manual too.
			logger.fatal("The specialization into a REPORTING_CONTAINER failed... Bad luck! (Weird though)");
			logger.fatal(e.getMessage());
		}

	} // end onCreateNode

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setNodeService(NodeService nodeService)	{
	    this.nodeService = nodeService;
	}
}
