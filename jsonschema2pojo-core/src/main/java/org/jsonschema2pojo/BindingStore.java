/**
 * Copyright © 2010-2014 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jsonschema2pojo;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.jsonschema2pojo.exception.GenerationException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BindingStore {

    private final List<Binding> bindings;

    public BindingStore() {
        this.bindings = new ArrayList<Binding>();
    }

    public BindingStore(GenerationConfig config) {
        this.bindings = Collections.unmodifiableList(parse(config.getBinding()));
    }

    private List<Binding> parse(URL url) {
        if (null == url) {
            return new ArrayList<Binding>();
        }
        ObjectMapper mapper = new ObjectMapper();
        Bindings bindings;
        try {
            bindings = mapper.readValue(url, Bindings.class);
        } catch (Exception e) {
            throw new GenerationException("Couldn't parse JSON bindings", e);
        }
        return bindings.getGlobal();
    }

    /**
     * Finds a binding where all the properties are matching. Will select the
     * entry with the most matching fields.
     * 
     * @param node
     *            the node to check for bindings
     * @return the binding or null if not found
     */
    public Binding findBinding(JsonNode node) {
        Binding bestMatch = null;
        long bestMatchPropertyCount = 0;
        for (Binding binding : bindings) {
            boolean matched = true;
            for (Entry<String, String> property : binding.getMatching().entrySet()) {
                if (!matches(node, property)) {
                    matched = false;
                }
            }
            if (matched && binding.getMatching().size() > bestMatchPropertyCount) {
                bestMatch = binding;
                bestMatchPropertyCount = binding.getMatching().size();
            }
        }

        return bestMatch;
    }

    private boolean matches(JsonNode node, Entry<String, String> matching) {
        if(!node.has(matching.getKey())){
            return false;
        }
        if(node.get(matching.getKey()).isArray()){
            for (Iterator<JsonNode> values = node.get(matching.getKey()).iterator(); values.hasNext();) {
                if(values.next().asText().equals(matching.getValue())){
                    return true;
                }
            }
            return false;
        }
        return node.get(matching.getKey()).asText().equals(matching.getValue());
    }
}
