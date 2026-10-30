import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ClienteService } from './cliente-service';
import { Cliente } from './cliente.model';
import { AuthService } from '../../core/auth/auth-service';
import { mensajeDeError } from '../../core/http-error';
import { Icono } from '../../shared/icono';

@Component({
  selector: 'app-perfil',
  imports: [ReactiveFormsModule, Icono],
  templateUrl: './perfil.html',
  styles: `
    .estrecha { max-width: 880px; }
    .titulo { font-size: clamp(2.6rem, 6vw, 4.4rem); margin-top: 0.3rem; }
    .aviso { margin-bottom: 1.2rem; }
    .bloque { margin-bottom: 1.4rem; }
    .bloque h3 { font-size: 1.5rem; margin-bottom: 1rem; }
    .acciones { display: flex; justify-content: flex-end; margin-top: 1.2rem; }
    .ficha { display: flex; gap: 1.3rem; align-items: center; }
    .ficha h3 { margin-bottom: 0.2rem; }
    .ficha p { margin: 0; }
    .avatar {
      flex: none; width: 72px; height: 72px; border-radius: 50%; display: grid; place-items: center;
      background: var(--tinta); color: var(--acento); font-family: var(--display); font-size: 1.8rem;
    }
    .talla { margin-top: 0.4rem !important; }
    .direccion { display: flex; gap: 0.9rem; align-items: flex-start; padding: 0.9rem 0; border-bottom: 1px solid var(--linea); }
    .direccion p { margin: 0.2rem 0 0; }
    .nueva { margin-top: 1.4rem; }
  `,
})
export class Perfil implements OnInit {
  private readonly clientes = inject(ClienteService);
  private readonly fb = inject(FormBuilder);
  protected readonly auth = inject(AuthService);

  protected readonly cliente = signal<Cliente | null>(null);
  protected readonly sinPerfil = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly perfilForm = this.fb.nonNullable.group({
    nombres: ['', [Validators.required, Validators.maxLength(80)]],
    apellidos: ['', [Validators.required, Validators.maxLength(80)]],
    dni: ['', Validators.pattern(/^\d{8}$/)],
    telefono: ['', Validators.pattern(/^9\d{8}$/)],
  });

  protected readonly direccionForm = this.fb.nonNullable.group({
    alias: ['Casa', Validators.required],
    direccion: ['', Validators.required],
    distrito: ['', Validators.required],
    ciudad: ['Lima', Validators.required],
    referencia: [''],
  });

  ngOnInit(): void {
    this.cargar();
  }

  private cargar(): void {
    this.clientes.miPerfil().subscribe({
      next: (c) => {
        this.cliente.set(c);
        this.sinPerfil.set(false);
      },
      error: (err: HttpErrorResponse) => {
        // 404 no es una falla: es "todavia no registraste tu perfil" (cliente-ms, S07)
        if (err.status === 404) this.sinPerfil.set(true);
        else this.error.set(mensajeDeError(err, 'No se pudo cargar tu perfil.'));
      },
    });
  }

  registrar(): void {
    if (this.perfilForm.invalid) return;
    const v = this.perfilForm.getRawValue();
    this.clientes.registrar({ ...v, dni: v.dni || null, telefono: v.telefono || null }).subscribe({
      next: () => this.cargar(),
      error: (err) => this.error.set(mensajeDeError(err, 'No se pudo registrar el perfil.')),
    });
  }

  agregarDireccion(): void {
    if (this.direccionForm.invalid) return;
    const v = this.direccionForm.getRawValue();
    this.clientes.agregarDireccion({ ...v, referencia: v.referencia || null, principal: true }).subscribe({
      next: () => {
        this.direccionForm.reset();
        this.cargar();
      },
      error: (err) => this.error.set(mensajeDeError(err, 'No se pudo guardar la direccion.')),
    });
  }
}
