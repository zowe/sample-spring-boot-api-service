/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.apiml;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.zowe.apiml.eurekaservice.client.config.Catalog;

/**
 * Represents one API Catalog UI tile (groups services together)
 *
 * @deprecated used to maintain the backward compatibility with the older version of APIML Java enabler
 */
@Deprecated
@Configuration
@ConfigurationProperties("apiml.service.catalog-ui-tile")
@Data
public class CatalogUiTile {
    private String id;
    private String title;
    private String description;
    private String version;

    public Catalog toCatalog() {
        Catalog.Tile tile = new Catalog.Tile(id, title, description, version);
        return new Catalog(tile);
    }
}
