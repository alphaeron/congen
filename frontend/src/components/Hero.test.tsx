import { render, screen } from "@testing-library/react";
import * as React from "react";

import { Hero } from "./Hero";

describe("Hero component", () => {
  it("Renders the catch phrase", () => {
    render(<Hero />);
    expect(
      screen.getByText(/Conjugate Method Programming, Without the Hastle/i),
    ).toBeInTheDocument();
  });
});
