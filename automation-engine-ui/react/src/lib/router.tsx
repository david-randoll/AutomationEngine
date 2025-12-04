import { useState, useEffect, createContext, useContext, useCallback, useMemo, type ReactNode } from "react";

type Route = {
    path: string;
    params: Record<string, string>;
};

type RouterContextType = {
    route: Route;
    navigate: (path: string) => void;
};

const RouterContext = createContext<RouterContextType | null>(null);

function parseHash(hash: string): Route {
    const path = hash.replace(/^#/, "") || "/";
    const params: Record<string, string> = {};

    // Extract query params if any
    const [pathname, search] = path.split("?");
    if (search) {
        const searchParams = new URLSearchParams(search);
        for (const [key, value] of searchParams) {
            params[key] = value;
        }
    }

    return { path: pathname, params };
}

export function RouterProvider({ children }: Readonly<{ children: ReactNode }>) {
    const [route, setRoute] = useState<Route>(() => parseHash(globalThis.location.hash));

    useEffect(() => {
        const handleHashChange = () => {
            setRoute(parseHash(globalThis.location.hash));
        };

        globalThis.addEventListener("hashchange", handleHashChange);
        return () => globalThis.removeEventListener("hashchange", handleHashChange);
    }, []);

    const navigate = useCallback((path: string) => {
        globalThis.location.hash = path;
    }, []);

    const contextValue = useMemo(() => ({ route, navigate }), [route, navigate]);

    return <RouterContext.Provider value={contextValue}>{children}</RouterContext.Provider>;
}

export function useRouter() {
    const context = useContext(RouterContext);
    if (!context) {
        throw new Error("useRouter must be used within a RouterProvider");
    }
    return context;
}

export function useRoute() {
    const { route } = useRouter();
    return route;
}

export function useNavigate() {
    const { navigate } = useRouter();
    return navigate;
}

interface LinkProps extends React.AnchorHTMLAttributes<HTMLAnchorElement> {
    to: string;
    children: ReactNode;
}

export function Link({ to, children, className, ...props }: Readonly<LinkProps>) {
    const navigate = useNavigate();

    const handleClick = (e: React.MouseEvent<HTMLAnchorElement>) => {
        e.preventDefault();
        navigate(to);
    };

    return (
        <a href={`#${to}`} onClick={handleClick} className={className} {...props}>
            {children}
        </a>
    );
}
