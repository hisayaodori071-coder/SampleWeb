(function () {
    const isCurrentMonth = document.body.dataset.currentMonth === "true";

    // 現在月を表示している場合だけ、1分ごとに再読み込みする。
    // これにより、退勤前・休憩中の勤務時間グラフが現在時刻まで伸びる。
    if (isCurrentMonth) {
        setInterval(function () {
            window.location.reload();
        }, 60 * 1000);
    }
})();