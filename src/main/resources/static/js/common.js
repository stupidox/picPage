function encode(str) {
    if (str && typeof(str) === 'string') {
        str = str.replaceAll('/', '\\');
        return str.replaceAll('\\', '%2f').replaceAll('[', '%5B').replaceAll(']', '%5D').replaceAll('+', '%2B')
            .replaceAll('&', '%26').replaceAll('▲', '%e2%96%b2').replaceAll('▼', '%e2%96%bc')
    }
    return ''
}

function decode(str) {
    str = str.replaceAll('/', '\\');
    if (str && typeof(str) === 'string') {
        return str.replaceAll('%2f', '\\').replaceAll('%5B', '[').replaceAll('%5D', ']').replaceAll('%2B', '+')
            .replaceAll('%26', '&').replaceAll('%e2%96%b2', '▲').replaceAll('%e2%96%bc', '▼')
    }
    return ''
}

function getParentName(str) {
    if (!str) {
        return '';
    }
    str = str.replaceAll('/', '\\');
    const i = str.lastIndexOf('\\');
    if (i > 0) {
        return str.substring(i+1);
    }
    return str;
}

function getParentPath(str) {
    if (!str) {
        return '';
    }
    str = str.replaceAll('/', '\\');
    const i = str.lastIndexOf('\\');
    if (i > 0) {
        return str.substring(0, i);
    }
    return str;
}