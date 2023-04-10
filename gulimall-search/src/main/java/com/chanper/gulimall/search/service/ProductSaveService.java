package com.chanper.gulimall.search.service;

import com.chanper.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;


public interface ProductSaveService {

    boolean saveProductAsIndices(List<SkuEsModel> skuEsModels) throws IOException;
}
