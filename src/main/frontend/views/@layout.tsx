import { createMenuItems, useViewConfig } from '@vaadin/hilla-file-router/runtime.js';
import { effect, signal } from '@vaadin/hilla-react-signals';
import { AppLayout, DrawerToggle, Icon, SideNav, SideNavItem } from '@vaadin/react-components';
import { Avatar } from '@vaadin/react-components/Avatar.js';
import { Button } from '@vaadin/react-components/Button.js';
import { useAuth } from 'Frontend/util/auth.js';
import { Suspense, useEffect } from 'react';
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
// @ts-ignore
import logo from '../../resources/META-INF/resources/img/gemba.png'; // Adjust the import path as needed

const defaultTitle = document.title;
const documentTitleSignal = signal('');
effect(() => {
    document.title = documentTitleSignal.value;
});

// Publish for Vaadin to use
(window as any).Vaadin.documentTitleSignal = documentTitleSignal;

export default function MainLayout() {
    const currentTitle = useViewConfig()?.title ?? defaultTitle;
    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        documentTitleSignal.value = currentTitle;
    }, [currentTitle]);

    const { state, logout } = useAuth();
    const profilePictureUrl =
        state.user &&   
        `data:image;base64,${btoa(
            state.user.profilePicture.reduce((str, n) => str + String.fromCharCode((n + 256) % 256), ''),
        )}`;

    return (
        <AppLayout primarySection="drawer">
            <div slot="drawer" className="flex flex-col justify-between h-full p-m">
                <header className="flex flex-col gap-m">
                    <div className="flex flex-col items-center gap-s">
                        <img src={logo} alt="Gemba Logo" className="h-1" style={{maxHeight: '40px'}}/>
                        <span className="font-semibold text-l">AI Platform</span>
                    </div>
                    <SideNav onNavigate={({path}) => navigate(path!)} location={location}>
                        {createMenuItems().map(({to, title, icon}) => (
                            <SideNavItem path={to} key={to}>
                                {icon ? <Icon src={icon} slot="prefix"></Icon> : <></>}
                                {title}
                            </SideNavItem>
                        ))}
                    </SideNav>
                </header>

                <footer className="flex flex-col gap-s">
                    {state.user ? (
                        <>
                            <div className="flex items-center gap-s">
                                <Avatar theme="xsmall" img={profilePictureUrl} name={state.user.name}/>
                                {state.user.name}
                            </div>
                            <Button
                                onClick={async () => {
                                    await logout();
                                    document.location.reload();
                                }}
                            >
                                Sign out
                            </Button>
                        </>
                    ) : (
                        <Link to="/login">Sign in</Link>
                    )}
                </footer>
            </div>

            <DrawerToggle slot="navbar" aria-label="Menu toggle"></DrawerToggle>
            <h1 slot="navbar" className="text-l m-0">
                {documentTitleSignal}
            </h1>

            <Suspense>
                <Outlet/>
            </Suspense>
        </AppLayout>
    );
}
