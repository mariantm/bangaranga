import { TestBed } from "@angular/core/testing";

import { ConsumeSpringSecurityService } from "./ConsumeSpringSecurityService";

describe("ConsumeSpringSecurityService", () => {
  let service: ConsumeSpringSecurityService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ConsumeSpringSecurityService);
  });

  it("should be created", () => {
    expect(service).toBeTruthy();
  });
});
