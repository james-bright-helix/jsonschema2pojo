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

package org.jsonschema2pojo.rules;

import org.jsonschema2pojo.Binding;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.exception.GenerationException;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JType;

/**
 * Applies the "binding" schema rule which allows overriding the generated type
 * based on matching the properties of the schema.
 */
public class BindingRule implements Rule<JClassContainer, JType> {

    private final RuleFactory ruleFactory;

    protected BindingRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    /**
     * Applies this rule to the given node to see if there is a matching binding
     * which should override the default type.
     *
     * @param nodeName
     *            the name of the node for which this "type" rule applies
     * @param node
     *            the node for which this "type" rule applies
     * @param jClassContainer
     *            the package into which any newly generated type may be placed
     * @return the Java type which, after reading the details of the given
     *         schema node, most appropriately matches the "type" specified
     */
    @Override
    public JType apply(String nodeName, JsonNode node, JClassContainer jClassContainer, Schema schema) {
        Binding binding = ruleFactory.getBindingStore().findBinding(node);
        JType boundType = null;
        if (binding != null) {
            try {
                boundType = jClassContainer.owner().ref(Class.forName(binding.getJavaType()));
            } catch (ClassNotFoundException e) {
                throw new GenerationException("Class specified for binding not found: " + binding, e);
            }
        }
        return boundType;
    }

}
