package cn.xunhou.web.xbbcloud.product.hrm.result;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author sha.li
 * @since 2022/9/14
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class ImportResult<T> implements Serializable {
    private static final long serialVersionUID = -5857685844606348739L;

    /**
     * 导入是否成功
     */
    private boolean success;
    /**
     * 文件id
     */
    private String fileId;

    /**
     * 文件url
     */
    private String fileUrl;
    private List<T> dataList;


}
