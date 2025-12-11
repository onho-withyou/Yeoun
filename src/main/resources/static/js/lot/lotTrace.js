// lotTrace.js 

document.addEventListener("DOMContentLoaded", () => {
  const treeEl = document.getElementById("lotTree");
  const detailEl = document.getElementById("lotDetail");

  if (!treeEl || !detailEl) return;

  treeEl.addEventListener("click", (e) => {
    const rootLink = e.target.closest(".lot-root-link");
    const groupToggle = e.target.closest(".lot-group-toggle");
    const processItem = e.target.closest(".process-item");

    // 1) ROOT 클릭 -> ROOT 하위 그룹(공정/자재/설비) 열기 + 오른쪽 LOT 개요
    if (rootLink) {
      e.preventDefault();

      const lotNo = rootLink.dataset.lotNo;

      // 같은 li 안의 root-children 찾기
      const li = rootLink.closest("li.list-group-item");
      const children = li.querySelector(".root-children");
      if (children) {
		
		// 1) 다른 LOT들의 children은 전부 접기
	    treeEl.querySelectorAll(".root-children").forEach(box => {
	      if (box !== children) {
	        box.classList.add("d-none");
	      }
	    });

	    // 2) 현재 클릭한 LOT만 토글 (같은 거 또 누르면 접어지게)
        children.classList.toggle("d-none");
      }

      // 오른쪽 LOT 전체 개요 (stepSeq 없이)
      loadLotDetail(lotNo, null);
      return;
    }

    // 2) 그룹(공정/자재/설비) 클릭 -> 해당 그룹 children 토글
    if (groupToggle) {
      e.preventDefault();

      const lotNo = groupToggle.dataset.lotNo;
      const group = groupToggle.dataset.group; // process / material / equipment

      const li = groupToggle.closest("li");
      const children = li.querySelector(`.group-children[data-group="${group}"]`);
      if (children) {
        children.classList.toggle("d-none");
      }
	  
	  // 공정 그룹
	  if (group === "process") {
		
		if (!children.dataset.loaded) {
		    loadProcessList(children, lotNo);
		    children.dataset.loaded = "true"; // 플래그 저장
		  }
	  }

      return;
    }

    // 3) 공정 아이템 클릭 -> 오른쪽 공정 상세
    if (processItem) {
      e.preventDefault();

      const lotNo = processItem.dataset.lotNo;
      const stepSeq = processItem.dataset.stepSeq;

      loadLotDetail(lotNo, stepSeq);
      return;
    }
  });

  function loadLotDetail(lotNo, stepSeq) {
    if (!lotNo) return;

    detailEl.innerHTML = "<div class='text-muted'>로딩 중...</div>";

    let url = `/lot/trace/detail?lotNo=${encodeURIComponent(lotNo)}`;
    if (stepSeq != null) {
      url += `&stepSeq=${encodeURIComponent(stepSeq)}`;
    }

	fetch(url)
	  .then(res => {
	    if (!res.ok) {
	      throw new Error("서버 오류: " + res.status);
	    }
	    return res.text();
	  })
	  .then(html => {
	    detailEl.innerHTML = html;
	  })
	  .catch(err => {
	    console.error(err);
	    detailEl.innerHTML =
	      "<div class='text-danger'>LOT 상세를 불러오는 중 오류가 발생했습니다.</div>";
	  });
  }
});

// 공정 리스트
function loadProcessList(container, lotNo) {

  if (!container) return;

  fetch(`/lot/trace/process-list?lotNo=${encodeURIComponent(lotNo)}`)
    .then(res => res.json())
    .then(list => {

      container.innerHTML = ""; // 기존 내용 비우기

      if (!list || list.length === 0) {
        container.innerHTML = "<li class='small text-muted'>(공정 정보 없음)</li>";
        return;
      }

      list.forEach(p => {
        const li = document.createElement("li");

        li.classList.add("process-item");
        li.dataset.lotNo = lotNo;
        li.dataset.stepSeq = p.stepSeq;

        li.innerHTML = `
          <a href="#" class="text-decoration-none small">
            ${p.processName} / ${p.processId}
            <span class="text-muted"> (${p.status})</span>
          </a>
        `;

        container.appendChild(li);
      });
    })
    .catch(err => {
      console.error(err);
      container.innerHTML =
        "<li class='small text-danger'>(공정 목록을 불러오는 중 오류가 발생했습니다)</li>";
    });
}