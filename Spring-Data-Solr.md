# Spring-Data-Solr

以下代码基于SpringBoot2.0.

### 1、搜索高亮显示业务代码

```java
//高亮显示
HighlightQuery query = new SimpleHighlightQuery();
//构建高亮显示对象
HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
highlightOptions.setSimplePrefix("<em style='color:red'>");
highlightOptions.setSimplePostfix("</em>");
//为查询对象设置高亮选项
query.setHighlightOptions(highlightOptions);
Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
query.addCriteria(criteria);
//高亮页对象
HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage("core1", query, TbItem.class);
//高亮入口集合
List<HighlightEntry<TbItem>> highlighted = highlightPage.getHighlighted();
for (HighlightEntry<TbItem> entry :
        highlighted) {
    //获取高亮列表(包含所有的高亮域)
    List<HighlightEntry.Highlight> list = entry.getHighlights();
    //for (HighlightEntry.Highlight highlight:    //每个高亮域可能有多个值
    //        list){
    //    List<String> snipplets = highlight.getSnipplets();
    //    System.out.println(snipplets);
    //}
    if (list.size() > 0 && list.get(0).getSnipplets().size() > 0){
        TbItem item = entry.getEntity();       
        item.setTitle(list.get(0).getSnipplets().get(0));
    }
}
map.put("rows",highlightPage.getContent()); // entry.getEntity() 和 highlightPage.getContent()是同一个引用，改变了entry.getEntity()就相当于改变highlightPage.getContent()
return map;
```



### 2、分组查询

```java
private List searchCategoryList(Map searchMap){
    List<String> list = new ArrayList();
    Query query = new SimpleQuery("*:*");
    //根据关键字查询
    Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));    //where..
    query.addCriteria(criteria);
    //设置分组选项
    GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category"); //group by
    groupOptions.setOffset(0);  //看源码发现必须有这两个属性才能从GroupOptions获取PageRequest对象，否则报错，Pageable must not be null!
    groupOptions.setLimit(0);
    System.out.println(groupOptions.getPageRequest());
    query.setGroupOptions(groupOptions);
    //分组页
    GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage("core1", query, TbItem.class);
    //获取分组结果对象
    GroupResult<TbItem> groupResult = groupPage.getGroupResult("item_category");
    //获取分组入口页
    Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
    //获取分组入口集合
    List<GroupEntry<TbItem>> content = groupEntries.getContent();

    for (GroupEntry entry:
         content) {
        //将分组结果添加到返回值中
        list.add(entry.getGroupValue());
    }
    return list;
}
```

注意：GroupOptions必须设置`offset`和`limit`属性，看源码发现必须有这两个属性才能从GroupOptions获取PageRequest对象，否则有如下异常：

`Pageable must not be null!`



### 3.过滤查询

```java
//1.2 商品分类过滤查询
if (!Objects.equals(searchMap.get("category"), "")){
    FilterQuery filterQuery = new SimpleQuery();
    Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
    filterQuery.addCriteria(filterCriteria);
    query.addFilterQuery(filterQuery);
}
```

### 4、按价格查询

```java
//1.5 按价格查询
if (!Objects.equals(searchMap.get("price"), "")){
    String price = (String) searchMap.get("price");
    String[] split = price.split("-");
    if (!Objects.equals(split[0], "0")){
        FilterQuery filterQuery = new SimpleQuery();
        Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(split[0]);
        filterQuery.addCriteria(filterCriteria);
        query.addFilterQuery(filterQuery);
    }
    if (!Objects.equals(split[1], "*")){
        FilterQuery filterQuery = new SimpleQuery();
        Criteria filterCriteria = new Criteria("item_price").lessThanEqual(split[1]);
        filterQuery.addCriteria(filterCriteria);
        query.addFilterQuery(filterQuery);
    }

}
```

### 5、分页

```java
//1.6 分页
Integer pageNo = (Integer) searchMap.get("pageNo");
if (pageNo == null){
    pageNo = 1;
}
Integer pageSize = (Integer) searchMap.get("pageSize");
if (pageSize == null){
    pageSize = 20;
}
query.setOffset((long) ((pageNo - 1) * pageSize));
query.setRows(pageSize);
```

### 6、按价格排序

```java
//1.7 按价格排序
String sortField = (String) searchMap.get("sortField");
String sort = (String) searchMap.get("sort");
if (sort != null && !Objects.equals(sort, "")){
    Sort querySort = new Sort(Sort.Direction.fromString(sort), sortField);
    query.addSort(querySort);
}
```

