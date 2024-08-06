document.getElementById('translateButton').addEventListener('click', async function() {
    const sourceLanguage = document.getElementById('sourceLanguage').value;
    const targetLanguage = document.getElementById('targetLanguage').value;
    const text = document.getElementById('text').value;

    if (!sourceLanguage || !targetLanguage || !text) {
        alert('Пожалуйста, заполните все поля.');
        return;
    }

    try {
        const response = await fetch('/api/translate?' + new URLSearchParams({
            sourceLanguage,
            targetLanguage,
            text
        }), {
            method: 'POST',
        });

        const data = await response.json();
        document.getElementById('resultStatus').innerText = 'HTTP Статус: ' + response.status;
        document.getElementById('resultText').innerText = data.responseText;
    } catch (error) {
        document.getElementById('resultText').innerText = 'Ошибка: ' + error.message;
    }
});
