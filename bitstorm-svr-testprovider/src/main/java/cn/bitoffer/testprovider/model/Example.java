package cn.bitoffer.testprovider.model;

import cn.bitoffer.testprovider.common.BaseModel;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * @author 狂飙训练营
 */
@Data
public class UrlMap extends BaseModel implements Serializable {
    private String id;
    private String long_url;
    private String short_url;
    private Date created_at;;
    /**
     * Id
     */
    private Long exampleId;

    /**
     * Name
     */
    private String exampleName;

    /**
     * Status
     */
    private Integer status;

    public Long getExampleId() {
        return exampleId;
    }

    public void setExampleId(Long exampleId) {
        this.exampleId = exampleId;
    }

    public String getExampleName() {
        return exampleName;
    }

    public void setExampleName(String exampleName) {
        this.exampleName = exampleName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Example{" +
                "exampleId=" + exampleId +
                ", exampleName='" + exampleName + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}


