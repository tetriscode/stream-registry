/**
 * Copyright (C) 2018-2019 Expedia, Inc.
 *
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
 */
package com.expediagroup.streamplatform.streamregistry.repository.avro;

import java.io.IOException;
import java.io.UncheckedIOException;

import lombok.RequiredArgsConstructor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hotels.beans.BeanUtils;
import com.hotels.beans.model.FieldMapping;
import com.hotels.beans.model.FieldTransformer;
import com.hotels.beans.transformer.Transformer;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AvroTransformer {
  private static final ObjectMapper mapper = new ObjectMapper();

  private final Transformer transformer;

  public AvroTransformer() {
    this(new BeanUtils()
        .getTransformer()
        .withFieldMapping(new FieldMapping("configurationString", "configuration"))
        .withFieldTransformer(new FieldTransformer<>("configuration", AvroTransformer::parseObjectNode))

        .withFieldMapping(new FieldMapping("configuration", "configurationString"))
        .withFieldTransformer(new FieldTransformer<>("configurationString", ObjectNode::toString))

        .withFieldMapping(new FieldMapping("domainAvroKey", "domainKey"))
        .withFieldTransformer(new FieldTransformer<>("domainKey", AvroDomainConversion::modelKey))

        .withFieldMapping(new FieldMapping("domainKey", "domainAvroKey"))
        .withFieldTransformer(new FieldTransformer<>("domainAvroKey", AvroDomainConversion::avroKey))

        .withFieldMapping(new FieldMapping("schemaAvroKey", "schemaKey"))
        .withFieldTransformer(new FieldTransformer<>("schemaKey", AvroSchemaConversion::modelKey))

        .withFieldMapping(new FieldMapping("schemaKey", "schemaAvroKey"))
        .withFieldTransformer(new FieldTransformer<>("schemaAvroKey", AvroSchemaConversion::avroKey)));
  }

  public <T, R> R transform(T sourceObj, Class<R> targetClass) {
    return transformer.transform(sourceObj, targetClass);
  }

  private static ObjectNode parseObjectNode(String value) {
    try {
      return (ObjectNode) mapper.readTree(value);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
