# 간단한 크롤링 데이터 이전 자동화 프로그램 [beta]

## 개요 
데이터 이전 할 게시판 글(게시판 내용, 이미지, 첨부파일, 참고링크)들을 </br>
한땀한땀 복붙복붙 노가다로 옮기라고 하셔서 그게 싫어서 만든 <br/>
크롤링 DB 이전 자동화 시스템, 주말에 집에서 혼자 끄적여서 만들었습니다. <br/>


### 작업시간
1. Beta 버전 : 6시간


### 개선점 [2024.07.28]

1. 프로젝트에 맞춰 하드코딩되어있는것 모두 수정해야함;
2. 5시간정도 작업을 더 진행하면 크롤링후 DB  insert가 가능한 유연한 프로그램이 완성 될것 같습니다.


### 주요 기능

1. DB세팅을 통해서 바로 인서트가 가능 (table 세팅 가능)
   2. 원하는 html tag 요소의 데이터와 테이블 컬럼과 매핑시켜 insert 가 가능

2. 게시판 내용 별로 추출해서 내용을 txt 파일로 추출하고 관련된 파일들을 뽑아내줌



### 사용방법


command 양식 

targetURI : 실행시킬 uri 주소
targetLoopNumber : 반복할 횟수

#### targetParamsSettingList (각 ${} 값에 들어갈 세팅값, ${}값에 맞춰 세팅값을 넣어주세요.)
1. index : 순서 , 몇번째 ${} 인지 
2. startParams : 시작값
3. addNumber : 값을 추가 시킬지 판단 여부 false면 addValue 가 있어도 추가가 안된다.
4. addValue : 추가될 값


nullableOrElseGet : null일때 대체할 값을 표기, ()
nullableOrPass : null 이면 insert 없이 pass할지 안할지 체크 (true면 insert 안하고 pass)




```json
{
   "targetURI": "https://www.naver.com/${}/${}&test?v=${}",
   "targetLoopNumber" : 20,
   "targetParamsSettingList" : [
      {
         "index" : 0,
         "startParams" : 1,
         "addNumber" : true,
         "addValue" : 1
      },
      {
         "index" : 1,
         "startParams" : 2,
         "addNumber" : true,
         "addValue" : 2
      },
      {
         "index" : 2,
         "startParams" : "test",
         "addNumber" : false,
         "addValue" : 0
      }
   ],
   "targetURIList": [
      "https://www.naver.com/1/2&test?v=3",
      "https://www.naver.com/1/3",
      "https://www.naver.com/1/4&test?v=5",
      "https://www.naver.com/2&test?v=${}",
      "https://www.naver.com/board/14",
      "https://www.naver.com"
   ],
   "tableName" : "testTable",
   "columnList" : ["board_title", "insert_date","file_group_id"],
   "linkElementObjectList" : [
      {
         "columnName" : "board_title",
         "element" :  "div",
         "elementFind" : "id",
         "elementName" : "board_tit",
         "file" : false,
         "nullableOrElseGet" : "데이터 이전 입니다.",
         "nullableOrPass" : false
      },
      {
         "columnName" : "board_content",
         "element" :  "div",
         "elementFind" : "id",
         "elementName" : "boardContent",
         "file" : false,
         "nullableOrElseGet" : "데이터 이전 입니다."
      },
      {
         "columnName" : "board_file",
         "element" :  "div",
         "elementFind" : "id",
         "elementName" : "board_tit",
         "file" : false,
         "nullableOrElseGet" : "데이터 이전 입니다."
      },
      {
         "columnName" : "file_group_id",
         "element" :  "p",
         "elementFind" : "class",
         "elementName" : "file-list",
         "file" : true,
         "openWindowDownload": true,
         "id" : "1"
      }
   ],
   "fileDownloadFilePath" : ""
}




```