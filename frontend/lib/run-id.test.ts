import { getNextRunId, getNextRunIds } from "@/lib/run-id";

describe("run-id helpers", () => {
  it("returns the next sequential id from mixed inputs", () => {
    expect(getNextRunId(["draft", "run_7", "run_12"])).toBe("run_13");
  });

  it("returns an empty list when count is zero or negative", () => {
    expect(getNextRunIds(["run_2"], 0)).toEqual([]);
    expect(getNextRunIds(["run_2"], -3)).toEqual([]);
  });

  it("allocates a contiguous range of run ids", () => {
    expect(getNextRunIds(["run_2", "run_8"], 3)).toEqual(["run_9", "run_10", "run_11"]);
  });
});
