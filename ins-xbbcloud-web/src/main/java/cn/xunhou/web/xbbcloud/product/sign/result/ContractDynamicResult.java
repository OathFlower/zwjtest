package cn.xunhou.web.xbbcloud.product.sign.result;


import cn.xunhou.grpc.proto.xbbcloud.SignServerProto;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Data
public class ContractDynamicResult {

    /**
     * 属性中文名称
     */
    private String propertyCNName;

    /**
     * 属性name
     */
    private String propertyName;

    /**
     * 属性value
     */
    private String propertyValue;

    /**
     * 属性下拉value(属性类型为下拉才有值)
     */
    private List<TextValueResult> propertySelectValue;

    /**
     * 属性类型name
     */
    private String propertyTypeName;

    /**
     * 属性类型code
     */
    private String propertyTypeCode;


    public static List<ContractDynamicResult> convert2Result(List<SignServerProto.ContractDynamicResponse> contractDynamicResponses) {
        List<ContractDynamicResult> contractDynamicResults = new ArrayList<>();
        if (CollectionUtils.isEmpty(contractDynamicResponses)) {
            return contractDynamicResults;
        }
        for (SignServerProto.ContractDynamicResponse contractDynamicResponse : contractDynamicResponses) {
            ContractDynamicResult contractDynamicForm = new ContractDynamicResult();
            List<TextValueResult> textValueForms = new ArrayList<>();
            List<SignServerProto.TextValueResult> propertySelectValueList = contractDynamicResponse.getPropertySelectValueList();
            for (SignServerProto.TextValueResult textValueDto : propertySelectValueList) {
                TextValueResult textValueForm = new TextValueResult();
                textValueForm.setText(textValueDto.getText());
                textValueForm.setValue(textValueDto.getValue());
                textValueForms.add(textValueForm);
            }
            contractDynamicForm.setPropertyName(contractDynamicResponse.getPropertyName());
            contractDynamicForm.setPropertyValue(contractDynamicResponse.getPropertyValue());
            contractDynamicForm.setPropertyTypeName(contractDynamicResponse.getPropertyTypeName());
            contractDynamicForm.setPropertyTypeCode(contractDynamicResponse.getPropertyTypeCode());
            contractDynamicForm.setPropertyCNName(contractDynamicResponse.getPropertyCNName());
            contractDynamicForm.setPropertySelectValue(textValueForms);
            contractDynamicResults.add(contractDynamicForm);
        }
        return contractDynamicResults;
    }
}
