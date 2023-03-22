#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.gui.components.compare;

import java.util.HashMap;
import java.util.Map;

public class ModelSpecs {

    public enum SpecType {
        TECHNOLOGY("Technology"),
        ANNOUNCED("Announced");

        private final String type;

        SpecType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    private final Map<SpecType, String> modelSpecsMap;

    public ModelSpecs() {
        this.modelSpecsMap = new HashMap<>();
    }

    public void setToModelSpecsMap(SpecType specType, String spec) {
        this.modelSpecsMap.put(specType, spec);
    }

    public String readSpec(SpecType specType) {
        return modelSpecsMap.get(specType);
    }
}
