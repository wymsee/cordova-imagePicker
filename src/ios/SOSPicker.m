//
//  SOSPicker.m
//  SyncOnSet
//
//  Created by Christopher Sullivan on 10/25/13.
//
//

#import "SOSPicker.h"

@implementation SOSPicker

@synthesize callbackId;

- (void) getPictures:(CDVInvokedUrlCommand *)command {
    self.callbackId = command.callbackId;
    NSDictionary *options = [command.arguments objectAtIndex: 0];
    NSInteger maximumImagesCount = [[options objectForKey:@"maximumImagesCount"] integerValue];
    NSString *uiThemeColor = [[[NSBundle mainBundle] objectForInfoDictionaryKey:@"uiThemeColor"] stringValue];
    
    UIColor *color = nil;
    NSString *photoSelImageName = nil;
    
    TZImagePickerController *imagePickerVc = [[TZImagePickerController alloc] initWithMaxImagesCount:maximumImagesCount delegate:self];
    
    if ([uiThemeColor isEqual:@"Yellow"]) {
        photoSelImageName = @"photo_sel_photoPickerVc_p";
        color = [UIColor colorWithRed:251 / 255.0 green:192 / 255.0 blue:45 / 255.0 alpha:1];
    } else if ([uiThemeColor isEqual:@"Blue"]) {
        photoSelImageName = @"photo_sel_photoPickerVc_t";
        color = [UIColor colorWithRed:97 / 255.0 green:170 / 255.0 blue:238 / 255.0 alpha:1];
    }
    
    if (color != nil) {
        imagePickerVc.oKButtonTitleColorNormal = color;
        imagePickerVc.iconThemeColor = color;
        imagePickerVc.naviBgColor = color;
        imagePickerVc.oKButtonTitleColorDisabled = [UIColor lightGrayColor];
        imagePickerVc.photoPreviewPageUIConfigBlock = ^(UICollectionView *collectionView, UIView *naviBar, UIButton *backButton, UIButton *selectButton, UILabel *indexLabel, UIView *toolBar, UIButton *originalPhotoButton, UILabel *originalPhotoLabel, UIButton *doneButton, UIImageView *numberImageView, UILabel *numberLabel) {
            naviBar.backgroundColor = color;
        };
    }
    
    if (photoSelImageName != nil) {
        // Must be set after setting the color
        imagePickerVc.photoSelImage = [UIImage tz_imageNamedFromMyBundle:photoSelImageName];
    }

    imagePickerVc.modalPresentationStyle = UIModalPresentationFullScreen;
    imagePickerVc.allowTakePicture = NO;
    imagePickerVc.allowCameraLocation = NO;
    imagePickerVc.allowTakeVideo = NO;
    imagePickerVc.allowPickingVideo = NO;
    imagePickerVc.allowPickingOriginalPhoto = NO;
    imagePickerVc.allowPickingGif = NO;

    dispatch_async(dispatch_get_main_queue(), ^{
        [self.viewController presentViewController:imagePickerVc animated:YES completion:nil];
    });
}

- (void) imagePickerController:(TZImagePickerController *)picker didFinishPickingPhotos:(NSArray<UIImage *> *)photos sourceAssets:(NSArray *)assets isSelectOriginalPhoto:(BOOL)isSelectOriginalPhoto infos:(NSArray<NSDictionary *> *)infos {
    CDVPluginResult *result = nil;
    
    NSMutableArray *resultPathArray = [[NSMutableArray alloc] init];
    NSString *tmpPath = [NSTemporaryDirectory() stringByStandardizingPath];
    
    NSData *data = nil;
    NSError *err = nil;
    
    NSString *filePath = nil;
    NSString *fileName = nil;
    
    for (UIImage *photo in photos) {
        fileName = [assets valueForKey:@"filename"];
        filePath = [NSString stringWithFormat:@"%@/%@%@", tmpPath, [[NSProcessInfo processInfo] globallyUniqueString], fileName];
        
        @autoreleasepool {
            data = UIImageJPEGRepresentation(photo, 1);
            if (![data writeToFile:filePath options:NSAtomicWrite error:&err]) {
                result = [CDVPluginResult resultWithStatus:CDVCommandStatus_IO_EXCEPTION messageAsString:[err localizedDescription]];
                break;
            } else {
                [resultPathArray addObject:[[NSURL fileURLWithPath:filePath] absoluteString]];
            }
        }
    }
    
    if (result == nil) {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:resultPathArray];
    }

    [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
}

- (void) tz_imagePickerControllerDidCancel:(TZImagePickerController *)picker {
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:[NSArray array]];
    [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
}

@end
