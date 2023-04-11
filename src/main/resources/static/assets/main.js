document.getElementById("lastCredits").addEventListener("change", async (e) => {
    if (!e.target.value) {
        document.getElementById("expectedCount").innerHTML = "?";
        return;
    }

    const response = await fetch("/api/count?from=" + e.target.value)

    let result = 0;

    if (response.status === 200) {
        const data = await response.json();
        result = data.expectedCount;
    }

    document.getElementById("expectedCount").innerHTML = result;
});

setInterval(() => {
    document.getElementById("lastCredits").dispatchEvent(new Event("change"));
}, 1000);
