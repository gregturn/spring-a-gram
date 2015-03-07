define(function (require) {

    return {
        wrapHref: wrapHref,
        wrapSelfHref: wrapSelfHref
    };

    // Take an href and serve it via obj.htmlUrl.href
    function wrapHref(href) {
        return {
            htmlUrl: {
                href: href
            }
        }
    }

    // Taken an href and serve it up via obj._links.self.href
    function wrapSelfHref(href) {
        return {
            _links: {
                self: {
                    href: href
                }
            }
        }
    }


});