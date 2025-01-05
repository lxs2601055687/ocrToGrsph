package com.example.ocrtograph.demos.web.bean;

import java.io.Serializable;
import lombok.Data;

/**
 *
 * @TableName graph_node
 */
@Data
public class GraphNode implements Serializable {
    /**
     * 节点ID
     */
    private Integer id;

    /**
     * 标签
     */
    private String label;

    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        GraphNode other = (GraphNode) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getLabel() == null ? other.getLabel() == null : this.getLabel().equals(other.getLabel()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getLabel() == null) ? 0 : getLabel().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", label=").append(label);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}