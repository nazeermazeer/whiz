// This javascript file gets what colors, background colors, 
// and type of display span elements will render.
// The contents of this file are fed into Viewer.java
// for color rendering.


(() => {
    const spans = document.querySelectorAll('span');
    const separator = String.fromCharCode(31);
    const inheritedColors = new WeakMap();

    // HtmlUnit can leave an explicitly inherited color as the
    // string "inherit". Walk up the DOM until the inherited
    // color is resolved to an actual RGB/RGBA value.
    function resolvedColor(element) {
        if (inheritedColors.has(element)) {
            return inheritedColors.get(element);
        }

        let current = element;
        let color;
        while (current) {
            color = window.getComputedStyle(current).color;
            if (color && color !== 'inherit') {
                color = rgbOnly(color);
                inheritedColors.set(element, color);
                return color;
            }
            current = current.parentElement;
        }

        // The browser default text color is black when no
        // ancestor supplies a color.
        color = 'rgb(0, 0, 0)';
        inheritedColors.set(element, color);
        return color;
    }

    // Keep the output format consistent by removing the
    // alpha channel from rgba(...) values.
    function rgbOnly(color) {
        return color.replace(
            /^rgba\\(\\s*([^,]+),\\s*([^,]+),\\s*([^,]+),\\s*[^)]+\\)$/i,
            'rgb($1, $2, $3)'
        );
    }

    return Array.from(spans, span => {
        const style = window.getComputedStyle(span);
        return resolvedColor(span) + separator
            + rgbOnly(style.backgroundColor) + separator
            + style.display;
    }).join(separator + separator);
})()