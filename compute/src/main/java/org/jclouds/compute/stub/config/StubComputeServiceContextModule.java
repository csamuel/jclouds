/**
 *
 * Copyright (C) 2010 Cloud Conscious, LLC. <info@cloudconscious.com>
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

package org.jclouds.compute.stub.config;

import org.jclouds.compute.ComputeServiceAdapter;
import org.jclouds.compute.config.JCloudsNativeStandaloneComputeServiceContextModule;
import org.jclouds.compute.domain.Hardware;
import org.jclouds.compute.domain.Image;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.concurrent.SingleThreaded;
import org.jclouds.domain.Location;

import com.google.inject.TypeLiteral;

/**
 * 
 * @author Adrian Cole
 */
@SingleThreaded
public class StubComputeServiceContextModule extends JCloudsNativeStandaloneComputeServiceContextModule {

   public StubComputeServiceContextModule() {
      super(StubComputeServiceAdapter.class);
   }

   @Override
   protected void configure() {
      install(new StubComputeServiceDependenciesModule());
      bind(new TypeLiteral<ComputeServiceAdapter<NodeMetadata, Hardware, Image, Location>>() {
      }).to(StubComputeServiceAdapter.class);
      super.configure();
   }
}
