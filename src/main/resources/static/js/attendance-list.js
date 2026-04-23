(function () {
    const MINUTES_PER_DAY = 24 * 60;

    function parseMinute(value) {
        if (value === undefined || value === null || value === "" || value === "null") {
            return null;
        }

        const number = Number(value);

        if (!Number.isFinite(number)) {
            return null;
        }

        return number;
    }

    function clamp(value, min, max) {
        return Math.min(Math.max(value, min), max);
    }

    function addSegment(graphArea, fromMinute, toMinute, type) {
        const from = clamp(fromMinute, 0, MINUTES_PER_DAY);
        const to = clamp(toMinute, 0, MINUTES_PER_DAY);

        if (to <= from) {
            return;
        }

        const segment = document.createElement("span");

        segment.className = "graph-segment " + type;

        const leftPercent = from * 100 / MINUTES_PER_DAY;
        const widthPercent = (to - from) * 100 / MINUTES_PER_DAY;

        segment.style.left = leftPercent + "%";
        segment.style.width = widthPercent + "%";

        graphArea.appendChild(segment);
    }

    function updateRealtimeGraphs() {
        const now = new Date();

        const nowMinute =
            now.getHours() * 60
            + now.getMinutes()
            + now.getSeconds() / 60;

        const graphAreas = document.querySelectorAll('.graph-area[data-today="true"]');

        graphAreas.forEach(function (graphArea) {
            const startMinute = parseMinute(graphArea.dataset.startMinute);
            const breakStartMinute = parseMinute(graphArea.dataset.breakStartMinute);
            const breakEndMinute = parseMinute(graphArea.dataset.breakEndMinute);
            const endMinute = parseMinute(graphArea.dataset.endMinute);

            // 出勤していない日は何もしない
            if (startMinute === null) {
                return;
            }

            // 退勤済みの日は、サーバー側で作ったグラフのままでよい
            if (endMinute !== null) {
                return;
            }

            const graphEndMinute = clamp(nowMinute, startMinute, MINUTES_PER_DAY);

            // 今日の未退勤グラフだけ作り直す
            graphArea.innerHTML = "";

            // 休憩開始があり、現在時刻より前の場合
            if (
                breakStartMinute !== null
                && breakStartMinute > startMinute
                && breakStartMinute < graphEndMinute
            ) {
                // 出勤 ～ 休憩開始
                addSegment(graphArea, startMinute, breakStartMinute, "work");

                // 休憩終了済みの場合
                if (
                    breakEndMinute !== null
                    && breakEndMinute > breakStartMinute
                    && breakEndMinute < graphEndMinute
                ) {
                    // 休憩開始 ～ 休憩終了
                    addSegment(graphArea, breakStartMinute, breakEndMinute, "break");

                    // 休憩終了 ～ 現在時刻
                    addSegment(graphArea, breakEndMinute, graphEndMinute, "work");
                } else {
                    // 休憩開始 ～ 現在時刻
                    addSegment(graphArea, breakStartMinute, graphEndMinute, "break");
                }

            } else {
                // 休憩なし：出勤 ～ 現在時刻
                addSegment(graphArea, startMinute, graphEndMinute, "work");
            }
        });
    }

    // 画面表示直後に1回更新
    updateRealtimeGraphs();

    // 10秒ごとにグラフだけ更新する
    // 画面全体は再読み込みしない
    setInterval(updateRealtimeGraphs, 10 * 1000);
})();