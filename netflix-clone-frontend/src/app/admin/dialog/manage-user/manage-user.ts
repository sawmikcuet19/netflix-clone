import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserService } from '../../../shared/shared/user-service';
import { NotificationService } from '../../../shared/shared/notification-service';
import { ErrorHandlerService } from '../../../shared/shared/error-handler-service';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { email } from '@angular/forms/signals';

@Component({
  selector: 'app-manage-user',
  standalone: false,
  templateUrl: './manage-user.html',
  styleUrl: './manage-user.css',
})
export class ManageUser {
  userForm! : FormGroup;
  creating = false;
  hidePassword = true;
  isEditMode: boolean = false;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private notification: NotificationService,
    private errorHandlerService: ErrorHandlerService,
    public dialogRef: MatDialogRef<ManageUser>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ){
    this.isEditMode = data.mode === 'edit';

    this.userForm = this.fb.group({
      fullName: [data.user?.fullName || '', Validators.required],
      email: [data.user?.email || '', [Validators.required, Validators.email]],
      password: ['',this.isEditMode? [] : [Validators.required, Validators.minLength(6)]],
      role: [data.user?.role || 'USER', Validators.required]
    });
  }

  onCancel() {
    this.dialogRef.close();
  }

  onSave() {
    this.creating = true;
    const formData = this.userForm.value;

    const data = {
      email: formData.email?.trim().toLowerCase(),
      password: formData.password,
      fullName: formData.fullName,
      role: formData.role
    };

    const op$ = this.isEditMode
    ? this.userService.updateUser(this.data.user.id, data)
    : this.userService.createUser(data);

    op$.subscribe({
      next: (response: any) => {
        this.creating = false;
        this.notification.success(response?.message);
        this.dialogRef.close(true);
      },
      error: (err) => {
        this.creating = false;
        this.errorHandlerService.handle(err, 'Failed to save user');
      }
    });
  }
}
