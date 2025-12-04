import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./app/globals.css";
import App from "./components/App";
import { RouterProvider } from "./lib/router";
import NavBar from "./components/NavBar";
import Router from "./components/Router";

const Main = () => {
    return (
        <RouterProvider>
            <App>
                <NavBar />
                <Router />
            </App>
        </RouterProvider>
    );
};

export default Main;

createRoot(document.getElementById("root")!).render(
    <StrictMode>
        <Main />
    </StrictMode>
);


