package com.minimarket.service.impl;

import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /** Prefijo de las contraseñas ya cifradas con BCrypt ($2a$, $2b$ o $2y$). */
    private static final String PREFIJO_BCRYPT = "$2";

    @Override
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Override
    public Usuario save(Usuario usuario) {
        String password = usuario.getPassword();
        // Evita volver a cifrar una contraseña ya cifrada (por ejemplo, al actualizar
        // otros campos del usuario sin modificar su contraseña).
        if (password != null && !password.startsWith(PREFIJO_BCRYPT)) {
            usuario.setPassword(passwordEncoder.encode(password));
        }
        return usuarioRepository.save(usuario);
    }

    @Override
    public void deleteById(Long id) {
        usuarioRepository.deleteById(id);
    }
}
