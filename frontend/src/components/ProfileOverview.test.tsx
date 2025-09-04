import { ThemeProvider, createTheme } from '@mui/material/styles'
import { render, screen, fireEvent } from '@testing-library/react'
import React from 'react'

import { ProfileOverview } from './ProfileOverview'
import type { User } from '../api/types'

// Mock the globals module
jest.mock('../globals', () => ({
  KEYCLOAK_URL: 'http://localhost:8080',
}))

// Mock the formatDate utility
jest.mock('../common/utils', () => ({
  formatDate: (date: Date | undefined) => {
    if (!date) return 'N/A'
    return date.toLocaleDateString()
  },
}))

// Mock window.location
const mockLocation = {
  href: '',
  origin: 'http://localhost:3000',
  pathname: '/profile',
}
Object.defineProperty(window, 'location', {
  value: mockLocation,
  writable: true,
})

// Mock sessionStorage
const mockSessionStorage = {
  setItem: jest.fn(),
  getItem: jest.fn(),
  removeItem: jest.fn(),
}
Object.defineProperty(window, 'sessionStorage', {
  value: mockSessionStorage,
  writable: true,
})

// Create a theme for testing
const theme = createTheme()

const renderWithTheme = (component: React.ReactElement) => {
  return render(<ThemeProvider theme={theme}>{component}</ThemeProvider>)
}

describe('ProfileOverview', () => {
  const mockUser: User = {
    keycloak_id: 'test-user-id',
    name: 'Test User',
    created_at: new Date('2024-01-01T00:00:00.000Z'),
    updated_at: new Date('2024-01-01T00:00:00.000Z'),
    roles: ['user'],
  }

  beforeEach(() => {
    jest.clearAllMocks()
    mockLocation.href = ''
  })

  it('renders the component with correct title', () => {
    renderWithTheme(<ProfileOverview user={mockUser} />)

    expect(screen.getByText('Profile Overview')).toBeInTheDocument()
  })

  it('displays user information correctly', () => {
    renderWithTheme(<ProfileOverview user={mockUser} />)

    expect(screen.getByText('Test User')).toBeInTheDocument()
    expect(screen.getByText(/Member since/)).toBeInTheDocument()
    expect(screen.getByText(/Roles:/)).toBeInTheDocument()
  })

  it('redirects to Keycloak when edit button is clicked', () => {
    renderWithTheme(<ProfileOverview user={mockUser} />)

    const editButton = screen.getByRole('button', { name: /edit profile/i })
    fireEvent.click(editButton)

    // Check that sessionStorage was set with the current path
    expect(mockSessionStorage.setItem).toHaveBeenCalledWith(
      'congen_redirect_after_profile_edit',
      '/profile'
    )

    // Check that we're redirecting to Keycloak
    expect(mockLocation.href).toContain('http://localhost:8080/realms/congen/account/#/personal-info')
    expect(mockLocation.href).toContain('redirect_uri=http%3A%2F%2Flocalhost%3A3000%2Fprofile-edit-redirect')
  })

  it('displays roles when user has roles', () => {
    const userWithRoles: User = {
      ...mockUser,
      roles: ['user', 'admin'],
    }

    renderWithTheme(<ProfileOverview user={userWithRoles} />)

    expect(screen.getByText(/Roles: user, admin/)).toBeInTheDocument()
  })

  it('handles user without roles gracefully', () => {
    const userWithoutRoles: User = {
      ...mockUser,
      roles: undefined,
    }

    renderWithTheme(<ProfileOverview user={userWithoutRoles} />)

    expect(screen.getByText('Test User')).toBeInTheDocument()
    expect(screen.queryByText(/Roles:/)).not.toBeInTheDocument()
  })

  it('handles missing created_at date', () => {
    const userWithoutDate: User = {
      ...mockUser,
      created_at: undefined as unknown as Date,
    }

    renderWithTheme(<ProfileOverview user={userWithoutDate} />)

    expect(screen.getByText(/Member since N\/A/)).toBeInTheDocument()
  })

  it('renders avatar with account circle icon', () => {
    renderWithTheme(<ProfileOverview user={mockUser} />)

    // Look for the AccountCircleIcon instead of img role
    const avatarIcon = screen.getByTestId('AccountCircleIcon')
    expect(avatarIcon).toBeInTheDocument()
  })
})
