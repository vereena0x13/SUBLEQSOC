// Generator : SpinalHDL v1.10.2a    git head : a348a60b7e8b6a455c72e1536ec3d74a2ea16935
// Component : SubleqSOC

`timescale 1ns/1ps

module SubleqSOC (
  input  wire          clk,
  input  wire          resetn
);

  reg        [15:0]   mem_spinal_port1;
  wire       [15:0]   subleq_1_io_bus_addr;
  wire       [15:0]   subleq_1_io_bus_wdata;
  wire                subleq_1_io_bus_write;
  wire                subleq_1_io_bus_valid;
  wire       [15:0]   _zz_mem_port;
  wire       [15:0]   _zz_mem_port_1;
  reg        [15:0]   sb_rdata;
  wire                is_mmio_rq;
  wire       [15:0]   _zz_sb_rdata;
  reg [15:0] mem [0:65535];

  assign _zz_mem_port = subleq_1_io_bus_addr;
  assign _zz_mem_port_1 = subleq_1_io_bus_wdata;
  always @(posedge clk) begin
    if(subleq_1_io_bus_valid) begin
      mem[_zz_mem_port] <= _zz_mem_port_1;
    end
  end

  always @(posedge clk) begin
    if(subleq_1_io_bus_valid) begin
      mem_spinal_port1 <= mem[_zz_sb_rdata];
    end
  end

  Subleq subleq_1 (
    .io_bus_addr  (subleq_1_io_bus_addr[15:0] ), //o
    .io_bus_rdata (sb_rdata[15:0]             ), //i
    .io_bus_wdata (subleq_1_io_bus_wdata[15:0]), //o
    .io_bus_write (subleq_1_io_bus_write      ), //o
    .io_bus_valid (subleq_1_io_bus_valid      ), //o
    .clk          (clk                        ), //i
    .resetn       (resetn                     )  //i
  );
  assign is_mmio_rq = ($signed(subleq_1_io_bus_addr) < $signed(16'h0));
  assign _zz_sb_rdata = subleq_1_io_bus_addr;
  always @(posedge clk) begin
    if(!is_mmio_rq) begin
      sb_rdata <= mem_spinal_port1;
    end
  end


endmodule

module Subleq (
  output wire [15:0]   io_bus_addr,
  input  wire [15:0]   io_bus_rdata,
  output wire [15:0]   io_bus_wdata,
  output wire          io_bus_write,
  output wire          io_bus_valid,
  input  wire          clk,
  input  wire          resetn
);
  localparam fsm_enumDef_BOOT = 3'd0;
  localparam fsm_enumDef_fetchA = 3'd1;
  localparam fsm_enumDef_fetchB = 3'd2;
  localparam fsm_enumDef_fetchC = 3'd3;
  localparam fsm_enumDef_fetchT0 = 3'd4;
  localparam fsm_enumDef_fetchT1 = 3'd5;
  localparam fsm_enumDef_execute = 3'd6;

  reg        [15:0]   regs_a;
  reg        [15:0]   regs_b;
  reg        [15:0]   regs_c;
  reg        [15:0]   regs_ip;
  reg        [15:0]   b_addr;
  reg        [15:0]   b_wdata;
  reg                 b_write;
  reg                 b_valid;
  reg        [15:0]   t0;
  reg        [15:0]   t1;
  reg        [15:0]   r;
  wire       [15:0]   nIp;
  wire       [15:0]   sub;
  wire                br;
  wire                fsm_wantExit;
  reg                 fsm_wantStart;
  wire                fsm_wantKill;
  reg        [2:0]    fsm_stateReg;
  reg        [2:0]    fsm_stateNext;
  wire                when_StateMachine_l237;
  wire                when_StateMachine_l237_1;
  wire                when_StateMachine_l237_2;
  wire                when_StateMachine_l237_3;
  wire                when_StateMachine_l237_4;
  wire                when_StateMachine_l237_5;
  `ifndef SYNTHESIS
  reg [55:0] fsm_stateReg_string;
  reg [55:0] fsm_stateNext_string;
  `endif


  `ifndef SYNTHESIS
  always @(*) begin
    case(fsm_stateReg)
      fsm_enumDef_BOOT : fsm_stateReg_string = "BOOT   ";
      fsm_enumDef_fetchA : fsm_stateReg_string = "fetchA ";
      fsm_enumDef_fetchB : fsm_stateReg_string = "fetchB ";
      fsm_enumDef_fetchC : fsm_stateReg_string = "fetchC ";
      fsm_enumDef_fetchT0 : fsm_stateReg_string = "fetchT0";
      fsm_enumDef_fetchT1 : fsm_stateReg_string = "fetchT1";
      fsm_enumDef_execute : fsm_stateReg_string = "execute";
      default : fsm_stateReg_string = "???????";
    endcase
  end
  always @(*) begin
    case(fsm_stateNext)
      fsm_enumDef_BOOT : fsm_stateNext_string = "BOOT   ";
      fsm_enumDef_fetchA : fsm_stateNext_string = "fetchA ";
      fsm_enumDef_fetchB : fsm_stateNext_string = "fetchB ";
      fsm_enumDef_fetchC : fsm_stateNext_string = "fetchC ";
      fsm_enumDef_fetchT0 : fsm_stateNext_string = "fetchT0";
      fsm_enumDef_fetchT1 : fsm_stateNext_string = "fetchT1";
      fsm_enumDef_execute : fsm_stateNext_string = "execute";
      default : fsm_stateNext_string = "???????";
    endcase
  end
  `endif

  assign io_bus_addr = b_addr;
  assign io_bus_wdata = b_wdata;
  assign io_bus_write = b_write;
  assign io_bus_valid = b_valid;
  assign nIp = ($signed(regs_ip) + $signed(16'h0001));
  assign sub = ($signed(t1) - $signed(t0));
  assign br = ($signed(sub) <= $signed(16'h0));
  assign fsm_wantExit = 1'b0;
  always @(*) begin
    fsm_wantStart = 1'b0;
    case(fsm_stateReg)
      fsm_enumDef_fetchA : begin
      end
      fsm_enumDef_fetchB : begin
      end
      fsm_enumDef_fetchC : begin
      end
      fsm_enumDef_fetchT0 : begin
      end
      fsm_enumDef_fetchT1 : begin
      end
      fsm_enumDef_execute : begin
      end
      default : begin
        fsm_wantStart = 1'b1;
      end
    endcase
  end

  assign fsm_wantKill = 1'b0;
  always @(*) begin
    fsm_stateNext = fsm_stateReg;
    case(fsm_stateReg)
      fsm_enumDef_fetchA : begin
        fsm_stateNext = fsm_enumDef_fetchB;
      end
      fsm_enumDef_fetchB : begin
        fsm_stateNext = fsm_enumDef_fetchC;
      end
      fsm_enumDef_fetchC : begin
        fsm_stateNext = fsm_enumDef_fetchT0;
      end
      fsm_enumDef_fetchT0 : begin
        fsm_stateNext = fsm_enumDef_fetchT1;
      end
      fsm_enumDef_fetchT1 : begin
      end
      fsm_enumDef_execute : begin
        fsm_stateNext = fsm_enumDef_fetchA;
      end
      default : begin
      end
    endcase
    if(fsm_wantStart) begin
      fsm_stateNext = fsm_enumDef_fetchA;
    end
    if(fsm_wantKill) begin
      fsm_stateNext = fsm_enumDef_BOOT;
    end
  end

  assign when_StateMachine_l237 = ((fsm_stateReg == fsm_enumDef_fetchA) && (! (fsm_stateNext == fsm_enumDef_fetchA)));
  assign when_StateMachine_l237_1 = ((fsm_stateReg == fsm_enumDef_fetchB) && (! (fsm_stateNext == fsm_enumDef_fetchB)));
  assign when_StateMachine_l237_2 = ((fsm_stateReg == fsm_enumDef_fetchC) && (! (fsm_stateNext == fsm_enumDef_fetchC)));
  assign when_StateMachine_l237_3 = ((fsm_stateReg == fsm_enumDef_fetchT0) && (! (fsm_stateNext == fsm_enumDef_fetchT0)));
  assign when_StateMachine_l237_4 = ((fsm_stateReg == fsm_enumDef_fetchT1) && (! (fsm_stateNext == fsm_enumDef_fetchT1)));
  assign when_StateMachine_l237_5 = ((fsm_stateReg == fsm_enumDef_execute) && (! (fsm_stateNext == fsm_enumDef_execute)));
  always @(posedge clk or negedge resetn) begin
    if(!resetn) begin
      regs_ip <= 16'h0;
      fsm_stateReg <= fsm_enumDef_BOOT;
    end else begin
      fsm_stateReg <= fsm_stateNext;
      if(when_StateMachine_l237) begin
        regs_ip <= nIp;
      end
      if(when_StateMachine_l237_1) begin
        regs_ip <= nIp;
      end
      if(when_StateMachine_l237_2) begin
        regs_ip <= nIp;
      end
      if(when_StateMachine_l237_3) begin
        regs_ip <= nIp;
      end
      if(when_StateMachine_l237_4) begin
        regs_ip <= nIp;
      end
      if(when_StateMachine_l237_5) begin
        if(br) begin
          regs_ip <= regs_c;
        end else begin
          regs_ip <= nIp;
        end
      end
    end
  end

  always @(posedge clk) begin
    case(fsm_stateReg)
      fsm_enumDef_fetchA : begin
        b_addr <= regs_ip;
        b_write <= 1'b0;
        b_valid <= 1'b1;
      end
      fsm_enumDef_fetchB : begin
        b_addr <= regs_ip;
        b_write <= 1'b0;
        b_valid <= 1'b1;
      end
      fsm_enumDef_fetchC : begin
        b_addr <= regs_ip;
        b_write <= 1'b0;
        b_valid <= 1'b1;
      end
      fsm_enumDef_fetchT0 : begin
        b_addr <= regs_a;
        b_write <= 1'b0;
        b_valid <= 1'b1;
      end
      fsm_enumDef_fetchT1 : begin
        b_addr <= regs_b;
        b_write <= 1'b0;
        b_valid <= 1'b1;
      end
      fsm_enumDef_execute : begin
        b_addr <= regs_b;
        b_wdata <= r;
        b_write <= 1'b1;
        b_valid <= 1'b1;
      end
      default : begin
      end
    endcase
    if(when_StateMachine_l237) begin
      b_valid <= 1'b0;
    end
    if(when_StateMachine_l237_1) begin
      b_valid <= 1'b0;
    end
    if(when_StateMachine_l237_2) begin
      b_valid <= 1'b0;
    end
    if(when_StateMachine_l237_3) begin
      b_valid <= 1'b0;
    end
    if(when_StateMachine_l237_4) begin
      b_valid <= 1'b0;
    end
    if(when_StateMachine_l237_5) begin
      b_write <= 1'b0;
      b_valid <= 1'b0;
    end
  end


endmodule
