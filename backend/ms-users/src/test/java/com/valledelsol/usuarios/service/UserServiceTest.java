package com.valledelsol.usuarios.service;

import com.valledelsol.usuarios.auth.JwtUtil;
import com.valledelsol.usuarios.dto.LoginRequest;
import com.valledelsol.usuarios.dto.RegisterRequest;
import com.valledelsol.usuarios.dto.UserDTO;
import com.valledelsol.usuarios.model.User;
import com.valledelsol.usuarios.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUserSuccess() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("passwd");
        request.setEmail("newuser@mail.com");
        request.setRegion("Region");
        request.setCommune("Commune");
        request.setPhone("123456");
        request.setRole("USER");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("newuser@mail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("passwd")).thenReturn("encoded-pwd");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        User saved = userService.registerUser(request);

        assertNotNull(saved);
        assertEquals(1L, saved.getId());
        assertEquals("newuser", saved.getUsername());
        assertEquals("encoded-pwd", saved.getPassword());
        assertEquals("user", saved.getRole().toLowerCase());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUserDuplicateUsername() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existing");
        request.setPassword("passwd");

        when(userRepository.findByUsername("existing")).thenReturn(Optional.of(new User()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser(request));
        assertEquals("Nombre de usuario ya existe", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testAuthenticateSuccess() {
        LoginRequest request = new LoginRequest();
        request.setUsername("loginuser");
        request.setPassword("secret");

        User mockUser = new User("loginuser", "encoded-pwd");
        when(userRepository.findByUsername("loginuser")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("secret", "encoded-pwd")).thenReturn(true);

        Optional<User> result = userService.authenticate(request);

        assertTrue(result.isPresent());
        assertEquals("loginuser", result.get().getUsername());
    }

    @Test
    void testAuthenticateFailure() {
        LoginRequest request = new LoginRequest();
        request.setUsername("loginuser");
        request.setPassword("wrongpassword");

        User mockUser = new User("loginuser", "encoded-pwd");
        when(userRepository.findByUsername("loginuser")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongpassword", "encoded-pwd")).thenReturn(false);

        Optional<User> result = userService.authenticate(request);

        assertTrue(result.isEmpty());
    }

    @Test
    void testAuthenticateByEmailSuccess() {
        LoginRequest request = new LoginRequest();
        request.setUsername("user@example.com");
        request.setPassword("secret");

        User mockUser = new User("loginuser", "encoded-pwd");
        mockUser.setEmail("user@example.com");
        when(userRepository.findByUsername("user@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("secret", "encoded-pwd")).thenReturn(true);

        Optional<User> result = userService.authenticate(request);

        assertTrue(result.isPresent());
        assertEquals("loginuser", result.get().getUsername());
    }

    @Test
    void testRegisterUserDuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("passwd");
        request.setEmail("user@mail.com");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("user@mail.com")).thenReturn(Optional.of(new User()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser(request));
        assertEquals("Correo ya registrado", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGetAllUsersForAdminSuccess() {
        User user = new User("testuser", "pwd");
        user.setId(2L);
        user.setRole("ADMINISTRATOR");

        when(jwtUtil.getRole("Bearer admin-token")).thenReturn("ADMINISTRATOR");
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDTO> result = userService.getAllUsersForAdmin("Bearer admin-token");

        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
    }

    @Test
    void testGetAllUsersForAdminForbidden() {
        when(jwtUtil.getRole("Bearer user-token")).thenReturn("USER");

        assertThrows(SecurityException.class,
                () -> userService.getAllUsersForAdmin("Bearer user-token"));
    }

    @Test
    void testGetAllUsersForAdminInvalidHeaderFormatThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.getAllUsersForAdmin("Token admin-token"));
        assertEquals("Falta cabecera de Autorización", ex.getMessage());
    }

    @Test
    void testUpdateUserRoleSuccess() {
        User user = new User("someuser", "pwd");
        user.setId(10L);
        user.setRole("USER");

        when(jwtUtil.getRole("Bearer admin-token")).thenReturn("ADMINISTRATOR");
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO result = userService.updateUserRole(10L, "BRIGADIST", "Bearer admin-token");

        assertNotNull(result);
        assertEquals("BRIGADIST", result.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testGetAllUsersForAdminMissingHeaderThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.getAllUsersForAdmin(null));
        assertEquals("Falta cabecera de Autorización", ex.getMessage());
    }

    @Test
    void testGetAllUsersForAdminInvalidTokenThrows() {
        when(jwtUtil.getRole("Bearer invalid-token")).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.getAllUsersForAdmin("Bearer invalid-token"));
        assertEquals("Token inválido o expirado", ex.getMessage());
    }

    @Test
    void testUpdateUserMissingHeaderThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(1L, null, "name", null, null, null, null, null, null));
        assertEquals("Falta cabecera de Autorización", ex.getMessage());
    }

    @Test
    void testUpdateUserInvalidHeaderFormatThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(1L, "Token invalid", "name", null, null, null, null, null, null));
        assertEquals("Falta cabecera de Autorización", ex.getMessage());
    }

    @Test
    void testUpdateUserInvalidTokenThrows() {
        when(jwtUtil.getUserId("Bearer invalid-token")).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(1L, "Bearer invalid-token", "name", null, null, null, null, null, null));
        assertEquals("Token inválido o expirado", ex.getMessage());
    }

    @Test
    void testUpdateUserNotOwnerOrAdminThrows() {
        when(jwtUtil.getUserId("Bearer user-token")).thenReturn(2L);
        when(jwtUtil.getRole("Bearer user-token")).thenReturn("USER");

        SecurityException ex = assertThrows(SecurityException.class,
                () -> userService.updateUser(1L, "Bearer user-token", "name", null, null, null, null, null, null));
        assertEquals("No tienes permisos para modificar este perfil", ex.getMessage());
    }

    @Test
    void testUpdateUserRoleRequiresAdministrator() {
        User user = new User("someuser", "pwd");
        user.setId(10L);

        when(jwtUtil.getRole("Bearer user-token")).thenReturn("USER");
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        SecurityException ex = assertThrows(SecurityException.class,
                () -> userService.updateUserRole(10L, "BRIGADIST", "Bearer user-token"));
        assertEquals("Acceso denegado: se requieren permisos de Administrador", ex.getMessage());
    }

    @Test
    void testUpdateUserSetsFieldsAndPassword() {
        User user = new User("someuser", "oldpwd");
        user.setId(10L);
        user.setRole("USER");

        when(jwtUtil.getUserId("Bearer user-token")).thenReturn(10L);
        when(jwtUtil.getRole("Bearer user-token")).thenReturn("USER");
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpassword")).thenReturn("encoded-newpwd");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO result = userService.updateUser(10L, "Bearer user-token", "newuser", "newpassword", "Region", "Commune", "new@mail.com", "123456", null);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("new@mail.com", result.getEmail());
        assertEquals("encoded-newpwd", user.getPassword());
    }

    @Test
    void testUpdateUserRoleWithAdminAllowsRoleChange() {
        User user = new User("someuser", "pwd");
        user.setId(10L);
        user.setRole("USER");

        when(jwtUtil.getUserId("Bearer admin-token")).thenReturn(1L);
        when(jwtUtil.getRole("Bearer admin-token")).thenReturn("ADMINISTRATOR");
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO result = userService.updateUser(10L, "Bearer admin-token", null, null, null, null, null, null, "BRIGADIST");

        assertNotNull(result);
        assertEquals("BRIGADIST", result.getRole());
    }

    @Test
    void testRegisterUserWithNullEmailUsesDefaultRole() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("simpleuser");
        request.setPassword("passwd");
        request.setEmail(null);
        request.setRole(null);

        when(userRepository.findByUsername("simpleuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("passwd")).thenReturn("encoded-pass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });

        User saved = userService.registerUser(request);

        assertNotNull(saved);
        assertEquals(2L, saved.getId());
        assertEquals("simpleuser", saved.getUsername());
        assertNull(saved.getEmail());
        assertEquals("USER", saved.getRole());
    }

    @Test
    void testUpdateUserNotFoundThrows() {
        when(jwtUtil.getUserId("Bearer admin-token")).thenReturn(1L);
        when(jwtUtil.getRole("Bearer admin-token")).thenReturn("ADMINISTRATOR");
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.updateUser(5L, "Bearer admin-token", "name", null, null, null, null, null, null));
        assertEquals("Usuario no encontrado", ex.getMessage());
    }

    @Test
    void testUpdateUserOwnerDoesNotChangeRoleAndKeepsPasswordWhenNull() {
        User user = new User("owner", "oldpwd");
        user.setId(20L);
        user.setRole("BRIGADIST");

        when(jwtUtil.getUserId("Bearer owner-token")).thenReturn(20L);
        when(jwtUtil.getRole("Bearer owner-token")).thenReturn("BRIGADIST");
        when(userRepository.findById(20L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO result = userService.updateUser(20L, "Bearer owner-token", "owner2", null, "NewRegion", "NewCommune", "new@mail.com", "987654", "ADMINISTRATOR");

        assertNotNull(result);
        assertEquals("owner2", result.getUsername());
        assertEquals("new@mail.com", result.getEmail());
        assertEquals("BRIGADIST", result.getRole());
        assertEquals("oldpwd", user.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void testUpdateUserRoleNotFoundThrows() {
        when(jwtUtil.getRole("Bearer admin-token")).thenReturn("ADMINISTRATOR");
        when(userRepository.findById(11L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.updateUserRole(11L, "USER", "Bearer admin-token"));
        assertEquals("Usuario no encontrado", ex.getMessage());
    }
}
