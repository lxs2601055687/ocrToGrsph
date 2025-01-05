package com.example.ocrtograph.demos.web.utils;

import com.tencent.tcvectordb.client.VectorDBClient;
import com.tencent.tcvectordb.model.AIDatabase;
import com.tencent.tcvectordb.model.CollectionView;
import com.tencent.tcvectordb.model.param.collection.FieldType;
import com.tencent.tcvectordb.model.param.collection.FilterIndex;
import com.tencent.tcvectordb.model.param.collection.IndexType;
import com.tencent.tcvectordb.model.param.collectionView.CreateCollectionViewParam;
import com.tencent.tcvectordb.model.param.collectionView.LoadAndSplitTextParam;
import com.tencent.tcvectordb.model.param.database.ConnectParam;
import com.tencent.tcvectordb.model.param.enums.ReadConsistencyEnum;
import com.tencent.tcvectordb.client.RPCVectorDBClient;
import java.util.HashMap;
import java.util.Map;

public class tcvUtil {

    static VectorDBClient client = new RPCVectorDBClient(ConnectParam.newBuilder()
            .withUrl("http://lb-7lkdphk6-g0wre6at19x5vluc.clb.ap-guangzhou.tencentclb.com:50000")
            .withUsername("root")
            .withKey("y1iPxlOUv94K73HO6D3nLwTnZCwzU8f06KrzchNc")
            .withTimeout(30)
            .build(), ReadConsistencyEnum.EVENTUAL_CONSISTENCY);
    public static void uploadTcv() throws Exception {
        // 创建VectorDB Client
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withUrl("http://lb-7lkdphk6-g0wre6at19x5vluc.clb.ap-guangzhou.tencentclb.com:50000")
                .withUsername("root")
                .withKey("y1iPxlOUv94K73HO6D3nLwTnZCwzU8f06KrzchNc")
                .withTimeout(30)
                .build();
        VectorDBClient client = new RPCVectorDBClient(connectParam, ReadConsistencyEnum.EVENTUAL_CONSISTENCY);

        // 创建数据库
      /*  client.createAIDatabase("db-test-ai");*/

        // 获取数据库实例
        AIDatabase db = client.aiDatabase("db-test-ai");

        // 创建集合视图
       /* CreateCollectionViewParam collectionParam = CreateCollectionViewParam.newBuilder()
                .withName("coll-ai-files2")
                .withDescription("This is a collectionView")
                .addField(new FilterIndex("author", FieldType.String, IndexType.FILTER))
                .build();
        db.createCollectionView(collectionParam);
*/
        // 描述集合视图
        CollectionView collection = db.describeCollectionView("coll-ai-files2");

        // 上传文件
        for (int i = 1; i < 90; i++) {
            System.out.println("D:/Desktop/毕业设计/ocr/output_part_"+ i +".pdf");
            LoadAndSplitTextParam param = LoadAndSplitTextParam.newBuilder()
                    .withLocalFilePath("D:/Desktop/毕业设计/ocr/output_part_"+ i +".pdf")
                    .Build();
            Map<String, Object> metaDataMap = new HashMap<>();
            metaDataMap.put("author", "Tencent");
            collection.loadAndSplitText(param, metaDataMap);
        }
    }

    public static AIDatabase getAIDatabase( ) {

        AIDatabase db = client.aiDatabase("db-test-ai");
        return db;
    }

    public static void main(String[] args) {
        try {
            uploadTcv();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
