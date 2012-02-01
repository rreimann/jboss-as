/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.test.smoke.embedded.deployment.rar.examples;

import static org.junit.Assert.assertNotNull;
import static org.jboss.as.test.smoke.embedded.deployment.rar.examples.ResourceAdapterTestUtilities.XmlToRAModelOperations;
import static org.jboss.as.test.smoke.embedded.deployment.rar.examples.ResourceAdapterTestUtilities.operationListToCompositeOperation;
import static org.jboss.as.test.smoke.embedded.deployment.rar.examples.ResourceAdapterTestUtilities.readResource;

import org.jboss.as.test.smoke.embedded.deployment.rar.MultipleAdminObject1;
import org.jboss.as.test.smoke.embedded.deployment.rar.MultipleConnectionFactory1;
import org.jboss.as.test.integration.management.base.AbstractMgmtTestBase;
import org.jboss.as.test.integration.management.util.MgmtOperationException;
import javax.annotation.Resource;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.junit.*;
import org.junit.runner.RunWith;
import org.jboss.dmr.*;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;
import java.util.List;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;



/**
 * @author <a href="robert.reimann@googlemail.com">Robert Reimann</a>
 *         Deployment of a RAR packaged inside an EAR.
 */
@RunWith(Arquillian.class)
public class EarPackagedDeploymentTestCase extends AbstractMgmtTestBase {

	//@BeforeClass - called from @Deployment
	public static void setUp() throws Exception{
		initModelControllerClient("localhost",9999);
	    String xml=readResource("../test-classes/config/ear_packaged.xml");
        List<ModelNode> operations=XmlToRAModelOperations(xml);
        executeOperation(operationListToCompositeOperation(operations));

	}
	@AfterClass
	public static void tearDown() throws Exception{

		final ModelNode address = new ModelNode();
        address.add("subsystem", "resource-adapters");
        address.add("resource-adapter","ear_packaged.ear#ear_packaged.rar");
        address.protect();

        remove(address);
        closeModelControllerClient();

	}

    /**
     * Define the deployment
     *
     * @return The deployment archive
     */
   @Deployment
    public static EnterpriseArchive createDeployment()  throws Exception{
    	setUp();

        String deploymentName = "ear_packaged.ear";
        String subDeploymentName = "ear_packaged.rar";

        ResourceAdapterArchive raa =
                ShrinkWrap.create(ResourceAdapterArchive.class, subDeploymentName);
         JavaArchive ja = ShrinkWrap.create(JavaArchive.class,  "multiple.jar");
        ja.addPackage(MultipleConnectionFactory1.class.getPackage()).
        addClasses(EarPackagedDeploymentTestCase.class,AbstractMgmtTestBase.class,MgmtOperationException.class);
        raa.addAsLibrary(ja);

        raa.addAsManifestResource("rar/" + subDeploymentName + "/META-INF/ra.xml", "ra.xml")
        .addAsManifestResource(new StringAsset("Dependencies: org.jboss.as.controller-client,org.jboss.dmr,org.jboss.as.cli\n"),"MANIFEST.MF");

	    final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, deploymentName);
	    ear.addAsModule(raa);
	    ear.addAsManifestResource("rar/" + deploymentName + "/META-INF/application.xml", "application.xml");
	    //ear.as(ZipExporter.class).exportTo(new java.io.File("/Users/rr/Downloads/_tmp/ear_packaged.zip"));
        return ear;
    }

   @Resource(mappedName = "java:jboss/name1")
   private MultipleConnectionFactory1 connectionFactory1;


   @Resource(mappedName="java:jboss/Name3")
   private MultipleAdminObject1 adminObject1;


    /**
     * Test configuration
     *
     * @throws Throwable Thrown if case of an error
     */
    @Test
    public void testConfiguration() throws Throwable {

    	assertNotNull("CF1 not found",connectionFactory1);
    	assertNotNull("AO1 not found",adminObject1);
    }
}
