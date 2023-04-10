package com.chanper.gulimall.search.service;


import com.chanper.gulimall.search.vo.SearchParam;
import com.chanper.gulimall.search.vo.SearchResult;

public interface SearchService {
    SearchResult getSearchResult(SearchParam searchParam);
}
