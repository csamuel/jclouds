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

package org.jclouds.aws.ec2.compute.config;

import static org.jclouds.Constants.PROPERTY_SESSION_INTERVAL;
import static org.jclouds.compute.domain.OsFamily.AMZN_LINUX;
import static org.jclouds.compute.domain.OsFamily.CENTOS;
import static org.jclouds.compute.domain.OsFamily.UBUNTU;

import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.jclouds.aws.ec2.compute.EC2ComputeService;
import org.jclouds.aws.ec2.compute.domain.RegionAndName;
import org.jclouds.aws.ec2.compute.strategy.EC2DestroyLoadBalancerStrategy;
import org.jclouds.aws.ec2.compute.strategy.EC2LoadBalanceNodesStrategy;
import org.jclouds.aws.ec2.compute.suppliers.RegionAndNameToImageSupplier;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.config.BaseComputeServiceContextModule;
import org.jclouds.compute.domain.Image;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.strategy.DestroyLoadBalancerStrategy;
import org.jclouds.compute.strategy.LoadBalanceNodesStrategy;
import org.jclouds.rest.annotations.Provider;
import org.jclouds.rest.suppliers.RetryOnTimeOutButNotOnAuthorizationExceptionSupplier;

import com.google.common.base.Supplier;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;

/**
 * Configures the {@link ComputeServiceContext}; requires {@link EC2ComputeService} bound.
 * 
 * @author Adrian Cole
 */
public class EC2ComputeServiceContextModule extends BaseComputeServiceContextModule {
   @Override
   protected void configure() {
      install(new EC2ComputeServiceDependenciesModule());
      install(new EC2BindComputeStrategiesByClass());
      install(new EC2BindComputeSuppliersByClass());
      super.configure();
   }

   @Override
   protected TemplateBuilder provideTemplate(Injector injector, TemplateBuilder template) {
      String provider = injector.getInstance(Key.get(String.class, Provider.class));
      if ("eucalyptus".equals(provider))
         return template.osFamily(CENTOS);
      else if ("nova".equals(provider))
         return template.osFamily(UBUNTU);
      else
         return template.osFamily(AMZN_LINUX).os64Bit(true);
   }

   @Provides
   @Singleton
   protected Supplier<Map<RegionAndName, ? extends Image>> provideRegionAndNameToImageSupplierCache(
         @Named(PROPERTY_SESSION_INTERVAL) long seconds, final RegionAndNameToImageSupplier supplier) {
      return new RetryOnTimeOutButNotOnAuthorizationExceptionSupplier<Map<RegionAndName, ? extends Image>>(
            authException, seconds, new Supplier<Map<RegionAndName, ? extends Image>>() {
               @Override
               public Map<RegionAndName, ? extends Image> get() {
                  return supplier.get();
               }
            });
   }

   @Override
   protected void bindLoadBalancerService() {
      bind(LoadBalanceNodesStrategy.class).to(EC2LoadBalanceNodesStrategy.class);
      bind(DestroyLoadBalancerStrategy.class).to(EC2DestroyLoadBalancerStrategy.class);
   }
}
