import { render, screen } from '../../test-utils';
import { createMockKcContext } from '../../test-utils';
import Account from './Account';

describe('Account', () => {
  const defaultKcContext = createMockKcContext();

  it('renders user profile information correctly', () => {
    render(<Account kcContext={defaultKcContext} i18n={{}} />);

    expect(screen.getByText('User Profile')).toBeInTheDocument();
    expect(screen.getByText('Privacy & Data')).toBeInTheDocument();
    expect(screen.getByAltText('ConGen')).toBeInTheDocument();
  });

  it('renders app bar with navigation', () => {
    render(<Account kcContext={defaultKcContext} i18n={{}} />);

    expect(screen.getByAltText('ConGen')).toBeInTheDocument();
    expect(screen.getByText('Exercises')).toBeInTheDocument();
    expect(screen.getByText('Privacy')).toBeInTheDocument();
  });

  it('renders user drawer with profile sections', () => {
    render(<Account kcContext={defaultKcContext} i18n={{}} />);

    expect(screen.getByText('User Profile')).toBeInTheDocument();
    expect(screen.getByText('Privacy & Data')).toBeInTheDocument();
  });

  it('handles missing user information gracefully', () => {
    const kcContextWithoutUser = createMockKcContext({
      user: undefined,
    });

    render(<Account kcContext={kcContextWithoutUser} i18n={{}} />);

    expect(screen.getByText('User Profile')).toBeInTheDocument();
    expect(screen.getByText('Privacy & Data')).toBeInTheDocument();
  });

  it('handles partial user information', () => {
    const kcContextWithPartialUser = createMockKcContext({
      user: {
        username: 'testuser',
        email: 'test@example.com',
        firstName: 'John',
      },
    });

    render(<Account kcContext={kcContextWithPartialUser} i18n={{}} />);

    expect(screen.getByText('User Profile')).toBeInTheDocument();
    expect(screen.getByText('Privacy & Data')).toBeInTheDocument();
  });

  it('applies correct Material-UI styling', () => {
    render(<Account kcContext={defaultKcContext} i18n={{}} />);

    // Check for Material-UI components
    const appBar = document.querySelector('.MuiAppBar-root');
    expect(appBar).toBeInTheDocument();

    const drawer = document.querySelector('.MuiDrawer-root');
    expect(drawer).toBeInTheDocument();
  });

  it('renders with dark theme', () => {
    render(<Account kcContext={defaultKcContext} i18n={{}} />, {
      theme: 'dark',
    });

    expect(screen.getByText('User Profile')).toBeInTheDocument();
    expect(screen.getByText('Privacy & Data')).toBeInTheDocument();
  });

  it('displays user avatar in app bar', () => {
    render(<Account kcContext={defaultKcContext} i18n={{}} />);

    const avatar = document.querySelector('.MuiAvatar-root');
    expect(avatar).toBeInTheDocument();
  });

  it('handles long user names correctly', () => {
    const kcContextWithLongName = createMockKcContext({
      user: {
        username: 'testuser',
        email: 'test@example.com',
        firstName: 'VeryLongFirstName',
        lastName: 'VeryLongLastName',
      },
    });

    render(<Account kcContext={kcContextWithLongName} i18n={{}} />);

    expect(screen.getByText('User Profile')).toBeInTheDocument();
    expect(screen.getByText('Privacy & Data')).toBeInTheDocument();
  });

  it('renders navigation menu items', () => {
    render(<Account kcContext={defaultKcContext} i18n={{}} />);

    expect(screen.getByText('Exercises')).toBeInTheDocument();
    expect(screen.getByText('Privacy')).toBeInTheDocument();
  });

  it('maintains responsive design structure', () => {
    render(<Account kcContext={defaultKcContext} i18n={{}} />);

    // Check that the component has proper structure
    const appBar = document.querySelector('.MuiAppBar-root');
    expect(appBar).toBeInTheDocument();

    const drawer = document.querySelector('.MuiDrawer-root');
    expect(drawer).toBeInTheDocument();
  });
});
