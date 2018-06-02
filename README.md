# DMYPhotoGridView

`ExifInterface를 이용한 년,월,일 그룹핑 목록 만들기`

- RecyclerView
- GridLayoutManager : StickyHeaderGridLayoutManager로 grid 형태로 이미지 보여주
- sticky header : 년/월/일 헤더 표시
- AsyncTaskLoader : 이미지, 이미지 정보 가져오기
- Media.Store : uri 가져오기
- ExifInterface로 Date 가져오기

<br/>

**Pinch Zoom 했을 때 row값 변화**
|Daily|Month|Year|
|:---:|:---:|:--:|
|  3  |  5  | 7  |

## OpenSource
- [Glide](https://github.com/bumptech/glide) 4.7.1
- [Sticky Header Grid](https://github.com/Codewaves/Sticky-Header-Grid) 2.11.0
- [metadata extractor](https://github.com/drewnoakes/metadata-extractor) 0.9.6

<br/>

> [Glide Gallery 예제 참고](https://bumptech.github.io/glide/ref/samples.html)
