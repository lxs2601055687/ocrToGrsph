package com.example.ocrtograph.demos.web.utils;

import com.alibaba.dashscope.app.Application;
import com.alibaba.dashscope.app.ApplicationParam;
import com.alibaba.dashscope.app.ApplicationResult;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ocrtograph.demos.web.bean.GraphNode;
import com.example.ocrtograph.demos.web.bean.Relation;
import com.example.ocrtograph.demos.web.service.GraphNodeService;
import com.example.ocrtograph.demos.web.service.RelationService;
import com.example.ocrtograph.demos.web.service.impl.GraphNodeServiceImpl;
import com.example.ocrtograph.demos.web.service.impl.RelationServiceImpl;
import com.tencent.tcvectordb.model.AIDatabase;
import com.tencent.tcvectordb.model.CollectionView;
import com.tencent.tcvectordb.model.param.dml.GeneralParams;
import com.tencent.tcvectordb.model.param.dml.SearchByContentsParam;
import com.tencent.tcvectordb.model.param.entity.SearchContentInfo;
import org.springframework.beans.factory.annotation.Autowired;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class txtToGraphUtil {

    private static final ApplicationParam applicationParam = ApplicationParam.builder().appId("676f862f89c74e69b520a51e24108267").apiKey("sk-87c476dd5822400990fc97a3d706d55a").build();


    private static GraphNodeService graphNodeService = new GraphNodeServiceImpl();


    private static RelationService relationService = new RelationServiceImpl();
    //多线程处理
    public static void txtToGraph (String txtPath, String graphPath) throws NoApiKeyException, InputRequiredException {
        // 读取txt文件 按照句号划分句子存到一个线程安全的集合中
        ConcurrentLinkedQueue<String> segments = new ConcurrentLinkedQueue<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(txtPath))) {
            StringBuilder segmentBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                // 移除多余的空格
                line = line.trim();
                // 将当前行添加到StringBuilder中
                segmentBuilder.append(line);

                // 检查是否达到300个汉字
                while (countChineseCharacters(segmentBuilder) >= 300) {
                    // 提取前300个汉字作为一段
                    String segment = segmentBuilder.substring(0, findEndOfSegment(segmentBuilder, 300));
                    segments.add(segment);
                    // 移除已经添加为一段的汉字
                    segmentBuilder.delete(0, findEndOfSegment(segmentBuilder, 300));
                }
            }

            // 添加剩余的部分作为最后一段
            if (segmentBuilder.length() > 0) {
                segments.add(segmentBuilder.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 打印所有句子
        segments.forEach((sentence) -> {
            System.out.println( "______________________"+sentence);
        });
        // 多线程处理每个句子，每个句子先从向量数据库中找到对应的向量，然后根据相似度找到最相似的句子
        AIDatabase db = tcvUtil.getAIDatabase();

        CollectionView collection = db.describeCollectionView("coll-ai-files2");


        List<String> documents = new ArrayList<>();
        for (int i = 1; i < 90; i++) {
            documents.add("output_part_"+ i +".pdf");
        }

        String question ="从远古到现代,伴随人类的生存繁衍,医学探索疾病发生和发展规律,研究疾病预防和诊疗对策。在远方祭昧时代,先民在与自然灾害、猛兽、疾病的斗争中开始了医疗保健活动,逐步发现了一些可以沧疗疾病的药物和疗法,在生与死中不断积累经验,逐步形成了原始的经验型医学知识。我国古代文献4市王世纪》记载了伏羲氏“造书契以代结绳之政,画八卦以通神明之德,以类万物之情,所以六气六脂六脏,五行阴阳,四时水火升降得以有象,百病之理,得以有类…...乃尝昧百药而制九针,以拯天枉焕。司马迁的《史记》和朱熹的《纲鉴》记载了神农氏“尝百草,始有医药“。《通鉴外纪》记载了黄希所创之医,乃上穷下际,察五色,立五运,洞性命,纪阴阳,咨于岐伯而作《内经》。“战国至秦汉时期,历代许多医家广泛收集整理当时积累的医疗经验和思想,不断丰富增补汇集而成《黄帝内经》,这是我国古代经验型医学理论的代表文献";

        SearchByContentsParam searchByContentsParam = SearchByContentsParam.newBuilder()
                .withContent(question)
                .withDocumentSetName("output_part_1.pdf")
                .build();

        List<SearchContentInfo> searchRes = collection.search(searchByContentsParam);
        System.out.println("searchRes.size() = " + searchRes.size());
        System.out.println("searchRes = " + searchRes);
        int i = 0;
        for (SearchContentInfo doc : searchRes) {
            System.out.println("\tres" +(i++)+": "+ doc.toString());
        }
        Application application = new Application();
        //请求百炼大模型，根据提示词生成对应的点和边
        applicationParam.setPrompt("从这段话中提取知识图谱的点和边也就是关系你应当按照我的格式来生成 我只要边的就可以 "+ "label" + "是" + "边的" + "的标签，只支持单个。from 是边的起点，to是边的终点。 用点的标签来表示 我希望你的回复张这个样子1. 边 (labelR=爱, from=我, to=你)\n" +
                "2. 边 (label=\"\", from=\"\", to=\"\")\n" +
                question);
        ApplicationResult call = application.call(applicationParam);
        String text = call.getOutput().getText();
        String[] split = text.split("\n");
        String[] split1 = new String[split.length];
        for (String s : split) {
            //s包含”边“字保留
            if (s.contains("边")) {
                split1[i++] = s;
            }
        }
        for(String s : split1) {
            System.out.println(s);
            // 定义正则表达式模式
            String regex = "label=(.*?), from=(.*?), to=(.*)";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(s);

            // 如果找到匹配项，则提取值
            if (matcher.find()) {
                String label = matcher.group(1);
                String from = matcher.group(2);
                String to = matcher.group(3);
                //去掉空格
                to = to.trim();
                to = to.substring(0, to.length() - 1);
                System.out.println("label: " + label);
                System.out.println("from: " + from);
                System.out.println("to: " + to);
                //存入数据库中：先查找是否有相同的点，如果有则不生成新的点，如果有相同的边则不生成新的边
                //查询from和to是否在node表中
                LambdaQueryWrapper<GraphNode> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(GraphNode::getLabel, from);
                GraphNode one = graphNodeService.getOne(queryWrapper);
                GraphNode graphNode = null;
                if (one == null) {
                    //生成新的点
                    graphNode = new GraphNode();
                    graphNode.setLabel(from);
                    graphNodeService.save(graphNode);
                }
                LambdaQueryWrapper<GraphNode> queryWrapper2 = new LambdaQueryWrapper<>();
                queryWrapper.eq(GraphNode::getLabel, to);
                GraphNode one2 = graphNodeService.getOne(queryWrapper2);
                GraphNode graphNode2 = null;
                if (one2 == null) {
                    //生成新的点
                     graphNode2 = new GraphNode();
                    graphNode2.setLabel(to);
                    graphNodeService.save(graphNode2);
                }
                //查询from和to是否在relation表中
                LambdaQueryWrapper<Relation> queryWrapper3 = new LambdaQueryWrapper<>();
                queryWrapper3.eq(Relation::getFrom, graphNode==null?one.getId():graphNode.getId());
                queryWrapper3.eq(Relation::getTo,  graphNode2==null?one2.getId():graphNode2.getId());
                Relation one3 = relationService.getOne(queryWrapper3);
                if (one3 == null) {
                    //生成新的边
                    Relation relation = new Relation();
                    relation.setLabel(label);
                    relation.setFrom(graphNode==null?one.getId():graphNode.getId());
                    relation.setTo(graphNode2==null?one2.getId():graphNode2.getId());
                    relationService.save(relation);
                }
            } else {
                System.out.println("没有找到匹配项");
            }
        }
    }


    public static void main(String[] args) {
        try {
            txtToGraph("D:/Desktop/毕业设计/ocr/demo.txt", "D:/Desktop/毕业设计/ocr/graph");
        } catch (NoApiKeyException e) {
            e.printStackTrace();
        } catch (InputRequiredException e) {
            e.printStackTrace();
        }
    }

    private static int countChineseCharacters(StringBuilder sb) {
        int count = 0;
        for (int i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);
            // 检查字符是否是汉字（根据Unicode范围）
            if ((c >= 0x4E00 && c <= 0x9FA5) || (c >= 0x3400 && c <= 0x4DBF)) {
                count++;
            }
        }
        return count;
    }

    // 找到下一个段落的结束位置（最后一个汉字的索引）
    private static int findEndOfSegment(StringBuilder sb, int maxCharacters) {
        int count = 0;
        int index = 0;
        for (int i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);
            if ((c >= 0x4E00 && c <= 0x9FA5) || (c >= 0x3400 && c <= 0x4DBF)) {
                count++;
                index = i;
                if (count == maxCharacters) {
                    break;
                }
            }
        }
        return index + 1;
    }
}

