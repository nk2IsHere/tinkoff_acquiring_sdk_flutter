#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint tinkoff_acquiring_sdk.podspec' to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'tinkoff_acquiring_sdk'
  s.version          = '1.0.2'
  s.summary          = 'Tinkoff Acquiring SDK bindings for Flutter.'
  s.description      = <<-DESC
  Tinkoff Acquiring SDK bindings for Flutter.
                       DESC
  s.homepage         = 'https://github.com/nk2ishere/tinkoff_acquiring_sdk_flutter'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'nk2' => 'nick@nk2.eu' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.dependency 'Flutter'
  s.dependency 'TinkoffASDKCore'
  s.dependency 'TinkoffASDKUI'
  s.platform = :ios, '11.0'

  # Flutter.framework does not contain a i386 slice.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386' }
  s.swift_version = '5.0'

end
