// Lab 2: colore cada linha de codigo com o highlight.js.
// Roda no DOMContentLoaded porque o highlight.js e carregado no fim do layout, depois deste script.
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.linha-codigo code').forEach(function (codigo) {
        hljs.highlightElement(codigo);
    });
});
