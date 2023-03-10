/*
 * Copyright (c) 2023, SAP SE
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package io.vertx.router.test.base;

import io.vertx.core.Future;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.openapi.router.RouterBuilder;
import io.vertx.openapi.contract.OpenAPIContract;

import java.nio.file.Path;
import java.util.function.Function;

import static io.vertx.openapi.impl.Utils.readYamlOrJson;

public class RouterBuilderTestBase extends HttpServerTestBase {
  /**
   * Creates a HTTPServer based on the passed RouterBuilder.
   * <p></p>
   * <b>Note:</b> This method should only be called once during a test.
   *
   * @param pathToContract      Path to the related OpenAPI contract
   * @param modifyRouterBuilder Function that allows to modify the RouterBuilder generated by the OpenAPI contract.
   * @return A Future which is succeeded when the server is started and failed if something went wrong.
   */
  protected Future<Void> createServer(Path pathToContract,
                                      Function<RouterBuilder, Future<RouterBuilder>> modifyRouterBuilder) {
    return createServer(pathToContract, contract -> RouterBuilder.create(vertx, contract), modifyRouterBuilder);
  }

  /**
   * Creates a HTTPServer based on the passed RouterBuilder.
   * <p></p>
   * <b>Note:</b> This method should only be called once during a test.
   *
   * @param pathToContract        Path to the related OpenAPI contract
   * @param routerBuilderSupplier Function that allows to create the RouterBuilder
   * @param modifyRouterBuilder   Function that allows to modify the RouterBuilder generated by the OpenAPI contract.
   * @return A Future which is succeeded when the server is started and failed if something went wrong.
   */
  protected Future<Void> createServer(Path pathToContract,
                                      Function<OpenAPIContract, RouterBuilder> routerBuilderSupplier,
                                      Function<RouterBuilder, Future<RouterBuilder>> modifyRouterBuilder) {

    return readYamlOrJson(vertx, pathToContract.toString())
      .compose(unresolvedContract -> OpenAPIContract.from(vertx, unresolvedContract).map(routerBuilderSupplier).compose(modifyRouterBuilder))
      .compose(rb -> {
        Router basePathRouter = Router.router(vertx);
        basePathRouter.route("/v1/*").subRouter(rb.createRouter());
        return super.createServer(basePathRouter);
      });
  }
}
