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

package org.jclouds.compute.config;

import static org.jclouds.Constants.PROPERTY_SESSION_INTERVAL;
import static org.jclouds.compute.domain.OsFamily.UBUNTU;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Named;
import javax.inject.Singleton;

import org.jclouds.collect.Memoized;
import org.jclouds.compute.LoadBalancerService;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.Hardware;
import org.jclouds.compute.domain.Image;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.functions.CreateSshClientOncePortIsListeningOnNode;
import org.jclouds.domain.Location;
import org.jclouds.rest.AuthorizationException;
import org.jclouds.rest.suppliers.RetryOnTimeOutButNotOnAuthorizationExceptionSupplier;
import org.jclouds.ssh.SshClient;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Providers;

/**
 * 
 * @author Adrian Cole
 */
public abstract class BaseComputeServiceContextModule extends AbstractModule {
   @Override
   protected void configure() {
      install(new ComputeServiceTimeoutsModule());
      bindLoadBalancerService();
      bind(new TypeLiteral<Function<NodeMetadata, SshClient>>() {
      }).to(CreateSshClientOncePortIsListeningOnNode.class);
   }

   protected void bindLoadBalancerService() {
      bind(LoadBalancerService.class).toProvider(Providers.<LoadBalancerService> of(null)).in(Scopes.SINGLETON);
   }

   /**
    * The default template if none is provided.
    */
   @Provides
   @Named("DEFAULT")
   protected TemplateBuilder provideTemplate(Injector injector, TemplateBuilder template) {
      return template.osFamily(UBUNTU);
   }

   /**
    * supplies how the tag is encoded into the name. A string of hex characters is the last argument
    * and tag is the first
    */
   @Provides
   @Named("NAMING_CONVENTION")
   @Singleton
   protected String provideNamingConvention() {
      return "%s-%s";
   }

   protected AtomicReference<AuthorizationException> authException = new AtomicReference<AuthorizationException>();

   @Provides
   @Singleton
   protected Supplier<Map<String, ? extends Image>> provideImageMap(@Memoized Supplier<Set<? extends Image>> images) {
      return Suppliers.compose(new Function<Set<? extends Image>, Map<String, ? extends Image>>() {

         @Override
         public Map<String, ? extends Image> apply(Set<? extends Image> from) {
            return Maps.uniqueIndex(from, new Function<Image, String>() {

               @Override
               public String apply(Image from) {
                  return from.getId();
               }

            });
         }

      }, images);
   }

   @Provides
   @Singleton
   @Memoized
   protected Supplier<Set<? extends Image>> supplyImageCache(@Named(PROPERTY_SESSION_INTERVAL) long seconds,
         final Supplier<Set<? extends Image>> imageSupplier) {
      return new RetryOnTimeOutButNotOnAuthorizationExceptionSupplier<Set<? extends Image>>(authException, seconds,
            new Supplier<Set<? extends Image>>() {
               @Override
               public Set<? extends Image> get() {
                  return imageSupplier.get();
               }
            });
   }

   @Provides
   @Singleton
   protected Supplier<Map<String, ? extends Location>> provideLocationMap(
         @Memoized Supplier<Set<? extends Location>> locations) {
      return Suppliers.compose(new Function<Set<? extends Location>, Map<String, ? extends Location>>() {

         @Override
         public Map<String, ? extends Location> apply(Set<? extends Location> from) {
            return Maps.uniqueIndex(from, new Function<Location, String>() {

               @Override
               public String apply(Location from) {
                  return from.getId();
               }

            });
         }

      }, locations);
   }

   @Provides
   @Singleton
   @Memoized
   protected Supplier<Set<? extends Location>> supplyLocationCache(@Named(PROPERTY_SESSION_INTERVAL) long seconds,
         final Supplier<Set<? extends Location>> locationSupplier) {
      return new RetryOnTimeOutButNotOnAuthorizationExceptionSupplier<Set<? extends Location>>(authException, seconds,
            new Supplier<Set<? extends Location>>() {
               @Override
               public Set<? extends Location> get() {
                  return locationSupplier.get();
               }
            });
   }

   @Provides
   @Singleton
   protected Supplier<Map<String, ? extends Hardware>> provideSizeMap(@Memoized Supplier<Set<? extends Hardware>> sizes) {
      return Suppliers.compose(new Function<Set<? extends Hardware>, Map<String, ? extends Hardware>>() {

         @Override
         public Map<String, ? extends Hardware> apply(Set<? extends Hardware> from) {
            return Maps.uniqueIndex(from, new Function<Hardware, String>() {

               @Override
               public String apply(Hardware from) {
                  return from.getId();
               }

            });
         }

      }, sizes);
   }

   @Provides
   @Singleton
   @Memoized
   protected Supplier<Set<? extends Hardware>> supplySizeCache(@Named(PROPERTY_SESSION_INTERVAL) long seconds,
         final Supplier<Set<? extends Hardware>> hardwareSupplier) {
      return new RetryOnTimeOutButNotOnAuthorizationExceptionSupplier<Set<? extends Hardware>>(authException, seconds,
            new Supplier<Set<? extends Hardware>>() {
               @Override
               public Set<? extends Hardware> get() {
                  return hardwareSupplier.get();
               }
            });
   }

   @Provides
   @Singleton
   protected Function<ComputeMetadata, String> indexer() {
      return new Function<ComputeMetadata, String>() {
         @Override
         public String apply(ComputeMetadata from) {
            return from.getProviderId();
         }
      };
   }

}