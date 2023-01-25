#ifndef VCAP_TYPES_H_
#define VCAP_TYPES_H_

#include <cstdint>
#include <functional>
#include <map>
#include <memory>
#include <string>

namespace vcap {

enum VCAPDeviceType {
  /*
   arm neon
  */
  VCAP_ARM_CPU        = 0,

  /*
   Gpu : OpenCL/OpenGLES/Vulkan, support Adreno/Mali
  */
  VCAP_OPENCL         = 1,

  VCAP_VULKAN         = 2,

  VCAP_OPENGLES       = 3,

  /*
   QualComm NN Hardware
  */
  VCAP_QUALCOMM_DSP   = 4,

  VCAP_QUALCOMM_NPU   = 5,

  /*
   MediaTek NN Hardware
  */
  VCAP_MEDIATEK_DSP   = 6,

  VCAP_MEDIATEK_APU   = 7,

  /*
   Samsung NN Hardware
  */
  VCAP_SAMSUNG_DSP    = 8,

  VCAP_SAMSUNG_NPU    = 9,

  VCAP_CUSTOM_RUNTIME = 10
};

enum ResCode {
  SUCCESS         =  0,
  PARSE_ERROR     = -1,
  FORWARD_FAILED  = -2,
  CPU_NOT_SUPPORT = -3,
  CPU_ALLOC_ERROR = -4,
  OPENCL_ERROR    = -5,
  GPU_ALLOC_ERROR = -6,
  HVX_NOT_SUPPORT = -7,
  HVX_ALLOC_ERROR = -8
};

enum DType {
  DT_INVALID = 0,
  DT_FLOAT32 = 1,
  DT_FLOAT16 = 2,
  DT_UINT8   = 3,
  DT_UINT16  = 4,
  DT_INT32   = 5,
  DT_INT8    = 6,
  DT_INT16   = 7
};

typedef struct {
  float scale        = 0.0f;
  int32_t zero_point = 0;
} QuantizationParams;

typedef struct {
  float* scale         = nullptr;
  int32_t* zero_point  = nullptr;
  int32_t channel_size = 0;
} PerchannelParams;

typedef struct {
  float min = 0.0f;
  float max = 0.0f;
} QuantizationMinmax;

enum ActivationType {
  NONE         = 0,
  RELU         = 1,
  RELU6        = 2,
  PRELU        = 3,
  TANH         = 4,
  SIGMOID      = 5,
  ELU          = 6,
  HARD_SWISH_X = 7,
  HARD_SWISH   = 8,
  GELU         = 9,
  SIGN_BIT     = 10
};

enum LossType {
  SoftMaxWithCrossEntropy = 0,
  BinaryCrossEntropy      = 1,
  SequenceLoss            = 2,
  MSE                     = 3,
};

enum OptimizeType {
  SGD      = 0,
  MOMENTUM = 1,
  ADAGRAD  = 2,
  ADAM     = 3,
};

enum OptimizeParamType {
  LearningRate = 0,
  WeightDecay  = 1,
};

enum OpenGLESVersion {
  GL_VER_3_1     = 0,
  GL_VER_3_2     = 1,
  GL_VER_UNKNOWN = 2
};

enum PowerMode{
  NOT_SETTING        = -1,
  NORMAL_PERFORMANCE =  0,
  LOW_PERFORMANCE    =  1,
  HIGH_PERFORMANCE   =  2
};

enum PrecisionMode {  // int8/fp16/fp32
  PRECISTION_INT8 = 0,
  PRECISION_FP16  = 1,
  PRECISION_FP32  = 2
};

enum GPUPriority{
  LOW_PRIORITY    = 0,
  NORMAL_PRIORITY = 1,
  HIGH_PRIORITY   = 2
};

enum MemStrategy {
  TRAINING    = 0,
  COMMON      = 1,
  SINGLEINOUT = 2
};

enum DataFormat {
  INVALID  = -1,
  NCHW     =  0,
  NC4HW4   =  1,
  NHWC     =  2,
  NC8HW8   =  3,
  NC16HW16 =  4,
  NC32HW32 =  5
};

enum QuantType {
  NOQUANT        = 0,
  QUANT_UINT8    = 1,
  QUANT_INT8     = 2,
  QUANT_INT16    = 3,
  QUANT_FP16FP32 = 4,
  QUANT_FP16     = 5,
  QUANT_INT4     = 6,
};

enum SrcFramework {
  UNKNOWN_FRAMEWORK = -1,
  TENSORFLOW        =  0,
  CAFFE             =  1,
  PYTORCH           =  2,
  ONNX              =  3,
  TFLITE            =  4,
};

enum LogLevel {
  VERBOSE  = 0,
  DEBUG    = 1,
  INFO     = 2,
  WARN     = 3,
  ERROR    = 4,
  NO_LOG   = 5
};

}  // namespace vcap

#endif