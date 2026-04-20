// 現在日時を画面に表示する関数
function updateDateTime() {
    // HTML側の id="currentDateTime" を取得
    const target = document.getElementById("currentDateTime");

    // 対象が見つからなければ何もしない
    if (!target) {
        return;
    }

    // 現在時刻を取得
    const now = new Date();

    // 日付部分を日本語形式で作る
    // 例: 2026/04/13(日)
    const datePart = now.toLocaleDateString("ja-JP", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        weekday: "short"
    });

    // 時刻部分を日本語形式で作る
    // 例: 14:30:05
    const timePart = now.toLocaleTimeString("ja-JP", {
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit"
    });

    // HTMLに反映
    // <br> で改行して、日付と時刻を2段表示にする
    target.innerHTML = `${datePart}<br>${timePart}`;
}

// 画面表示直後に1回実行
updateDateTime();

// 1秒ごとに更新して、時計がリアルタイムで進むようにする
setInterval(updateDateTime, 1000);