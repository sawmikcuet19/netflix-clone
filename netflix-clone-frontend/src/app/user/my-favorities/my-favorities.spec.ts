import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MyFavorities } from './my-favorities';

describe('MyFavorities', () => {
  let component: MyFavorities;
  let fixture: ComponentFixture<MyFavorities>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MyFavorities]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MyFavorities);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
