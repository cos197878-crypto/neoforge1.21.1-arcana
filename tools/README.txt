ScrollGen.java — 계열별 두루마리 텍스처 생성기

빌드에 포함되지 않는다 (src/ 밖에 있으므로 컴파일 대상이 아님).
색을 바꾸거나 계열을 추가할 때만 손으로 돌린다.

  cd tools
  javac ScrollGen.java
  java ScrollGen ../src/main/resources/assets/arcana/textures/item

계열 추가 순서:
  1. ScrollGen.java 의 SCHOOLS 배열에 한 줄 추가 후 위 명령 실행
  2. SchoolType.java 에 상수 추가 (modelIndex 는 다음 번호)
  3. models/item/scroll_<이름>.json 추가
  4. models/item/scroll.json 의 overrides 에 한 줄 추가
  5. lang 에 school.arcana.<이름> 추가
