import { render, screen } from "@testing-library/react";
import * as React from "react";

import { OpenSource } from "./OpenSource";

describe("OpenSource component", () => {
  it("Renders the correct license", () => {
    render(<OpenSource />);
    expect(
      screen.getByText("See details about license usage.").closest("a"),
    ).toHaveAttribute("href", "https://opensource.org/license/mit");
  });
});
