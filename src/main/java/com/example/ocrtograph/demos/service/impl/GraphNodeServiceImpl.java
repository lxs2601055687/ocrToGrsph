package com.example.ocrtograph.demos.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ocrtograph.demos.service.GraphNodeService;

import com.example.ocrtograph.demos.mapper.GraphNodeMapper;
import com.example.ocrtograph.demos.web.bean.GraphNode;
import org.springframework.stereotype.Service;

/**
* @author 26010
* @description 针对表【graph_node】的数据库操作Service实现
* @createDate 2025-01-04 16:23:57
*/
@Service
public class GraphNodeServiceImpl extends ServiceImpl<GraphNodeMapper, GraphNode>
    implements GraphNodeService {

}




