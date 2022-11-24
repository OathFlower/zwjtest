package cn.xunhou.web.xbbcloud.product.sign.result;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CustomerContractSubjectResult {

    private Long id;

    private List<SubjectConfigurationResult> subjectConfigurationDtoList;

    private Long CustomerContractId;


}
