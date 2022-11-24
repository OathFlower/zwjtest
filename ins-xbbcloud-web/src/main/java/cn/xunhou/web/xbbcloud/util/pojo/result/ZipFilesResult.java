package cn.xunhou.web.xbbcloud.util.pojo.result;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 压缩上传文件
 *
 * @author wangkm
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class ZipFilesResult {
    /**
     * 压缩文件
     */
    private String zipFileId;

    /**
     * 成功数
     */
    private Integer successCount;

    /**
     * 下载总数
     */
    private Integer allCount;
}
